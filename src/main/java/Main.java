import lombok.Cleanup;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        String configPath = args[0];
        OperationCollection operationCollection = OperationCollection.parse(configPath);

        if (operationCollection.isCreate())
            createDatabase(operationCollection);

        if (operationCollection.isLoad())
            loadData(operationCollection);

        System.out.println("[Executing SQLs]");
        dispatch(operationCollection);
    }

    @SneakyThrows
    private static void dispatch(OperationCollection operationCollection) {
        Map<String, Executor> executorMap = new HashMap<>();
        for (Operation operation : operationCollection.getOperationList()) {
            String trxId = operation.getTrxId();
            if (!executorMap.containsKey(trxId)) {
                executorMap.put(trxId, new Executor(trxId));
                executorMap.get(trxId).connect(
                        operationCollection.getConnUrl(),
                        operationCollection.getUser(),
                        operationCollection.getPassword());
            }
            executorMap.get(trxId).execute(operation.getSql());
        }
        for (Executor executor : executorMap.values()) {
            executor.close();
        }
    }

    private static void loadData(OperationCollection operationCollection) throws SQLException {
        System.out.printf("[Initiating database %s]%n", operationCollection.getDatabase());
        executeSQLList(operationCollection, operationCollection.getLoadList());
    }

    private static void executeSQLList(OperationCollection operationCollection, List<String> sqlList) throws SQLException {
        @Cleanup Connection conn = DriverManager.getConnection(
                operationCollection.getConnUrl(),
                operationCollection.getUser(),
                operationCollection.getPassword());
        @Cleanup Statement stat = conn.createStatement();

        for (String sql : sqlList) {
            stat.execute(sql);
            System.out.printf("\t%s%n", sql);
        }
    }

    private static void createDatabase(OperationCollection operationCollection) throws SQLException {
        System.out.printf("[Recreating database %s]%n", operationCollection.getDatabase());
        @Cleanup Connection connWithoutDB = DriverManager.getConnection(
                operationCollection.getInitUrl(),
                operationCollection.getUser(),
                operationCollection.getPassword());
        @Cleanup Statement statWithoutDB = connWithoutDB.createStatement();
        statWithoutDB.execute(String.format("drop database if exists `%s`;", operationCollection.getDatabase()));
        statWithoutDB.execute(String.format("create database `%s`;", operationCollection.getDatabase()));

        executeSQLList(operationCollection, operationCollection.getCreateList());
    }
}
