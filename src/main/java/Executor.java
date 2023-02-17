import lombok.Cleanup;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Executor extends Thread {
    private final String executorId;
    private Connection conn;
    private BlockingQueue<String> sqlQueue;

    public Executor(String executorId) {
        this.executorId = executorId;
        this.sqlQueue = new LinkedBlockingQueue<>();
    }

    public void dispatch(String sql) throws InterruptedException {
        this.sqlQueue.put(sql);
    }

    public void connect(String url, String user, String password) throws SQLException {
        this.conn = DriverManager.getConnection(url, user, password);
        this.conn.setAutoCommit(false);
    }

    private void execute(String sql) throws SQLException {
        OperationType opType = OperationType.detectType(sql);
        switch (opType) {
            case SELECT:
                executeQuery(sql, opType);
                break;
            case INSERT:
            case UPDATE:
            case DELETE:
                executeUpdate(sql, opType);
                break;
            case SET:
                set(sql, opType);
                break;
            case COMMIT:
                commit();
                break;
        }
    }

    private void set(String sql, OperationType opType) throws SQLException {
        @Cleanup Statement stat = this.conn.createStatement();
        int count = stat.executeUpdate(sql);
        TraceCollector.collect(new Trace(this.executorId, sql, opType, count));
    }

    private void commit() throws SQLException {
        this.conn.commit();
        TraceCollector.collect(new Trace(this.executorId, OperationType.COMMIT));
    }

    public void close() throws SQLException {
        this.conn.close();
    }

    private void executeQuery(String sql, OperationType operationType) throws SQLException {
        @Cleanup Statement stat = this.conn.createStatement();
        ResultSet resultSet = stat.executeQuery(sql);
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<String> colNames = new ArrayList<>();
        List<List<String>> colValuesList = new ArrayList<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            colNames.add(metaData.getColumnLabel(i));
        }

        while (resultSet.next()) {
            List<String> colValues = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                colValues.add(resultSet.getString(i));
            }
            colValuesList.add(colValues);
        }

        TraceCollector.collect(new Trace(executorId, sql, operationType, colNames, colValuesList));
    }

    private void executeUpdate(String sql, OperationType type) throws SQLException {
        @Cleanup Statement stat = this.conn.createStatement();
        try {
            int count = stat.executeUpdate(sql);
            TraceCollector.collect(new Trace(executorId, sql, type, count));
        } catch (SQLException e) {
            TraceCollector.collect(new Trace(executorId, sql, type, e));
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            String sql = sqlQueue.take();
            this.execute(sql);
        }
    }
}
