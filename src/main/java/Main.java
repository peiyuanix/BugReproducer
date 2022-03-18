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
        Config config = Config.parse(configPath);

        if (config.isCreate())
            create(config);

        if (config.isLoad())
            loadData(config);

        System.out.println("[Executing SQLs]");
        dispatch(config);
    }

    @SneakyThrows
    private static void dispatch(Config config) {
        Map<String, Executor> executorMap = new HashMap<>();
        for (Operation operation : config.getOperationList()) {
            String trxId = operation.getTrxId();
            if (!executorMap.containsKey(trxId)) {
                executorMap.put(trxId, new Executor(trxId));
                executorMap.get(trxId).connect(
                        config.getConnUrl(),
                        config.getUser(),
                        config.getPassword());
            }
            executorMap.get(trxId).execute(operation.getSql());
        }
        for (Executor executor : executorMap.values()) {
            executor.close();
        }
    }

    private static void loadData(Config config) throws SQLException {
        System.out.printf("[Initiating database]%n");
        Connection conn = newConnection(config.getConnUrl(), config);
        executeSQLList(conn, config.getLoadList());
    }

    private static void executeSQLList(Connection conn, List<String> sqlList) throws SQLException {
        @Cleanup Statement stat = conn.createStatement();
        for (String sql : sqlList) {
            stat.execute(sql);
            System.out.printf("\t%s%n", sql);
        }
    }

    private static void create(Config config) throws SQLException {
        System.out.printf("[Creating database]%n");
        @Cleanup Connection connWithoutDB = newConnection(config.getInitUrl(), config);
        executeSQLList(connWithoutDB, config.getCreateDatabaseList());

        @Cleanup Connection conn = newConnection(config.getConnUrl(), config);
        executeSQLList(conn, config.getCreateTableList());
    }

    private static Connection newConnection(String connUrl, Config operationColl) throws SQLException {
        return DriverManager.getConnection(
                connUrl,
                operationColl.getUser(),
                operationColl.getPassword());
    }
}
