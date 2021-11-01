import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        String configPath = args[0];
        OperationCollection operationCollection = OperationCollection.parse(configPath);
        initDb(operationCollection);
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
            executorMap.get(trxId).execute(operation.getSql(), operation.getType());
        }
        for (Executor executor : executorMap.values()) {
            executor.close();
        }
    }

    @SneakyThrows
    private static void initDb(OperationCollection operationCollection) {
        Connection conn = DriverManager.getConnection(
                operationCollection.getInitUrl(),
                operationCollection.getUser(),
                operationCollection.getPassword());
        Statement stat = conn.createStatement();
        stat.execute(String.format("drop database if exists `%s`;", operationCollection.getDatabase()));
        stat.execute(String.format("create database `%s`;", operationCollection.getDatabase()));
        stat.close();
        conn.close();

        conn = DriverManager.getConnection(
                operationCollection.getConnUrl(),
                operationCollection.getUser(),
                operationCollection.getPassword());
        stat = conn.createStatement();

        for (String sql : operationCollection.getSqlList()) {
            stat.execute(sql);
        }
    }
}
