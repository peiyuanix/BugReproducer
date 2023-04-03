import java.util.Locale;

public enum OperationType {
    SET,
    INSERT,
    DELETE,
    SELECT,
    UPDATE,
    COMMIT,
    UNKNOWN, BEGIN;

    public static OperationType detectType(String sql) {
        String firstKeyword = sql.split(" ")[0].toLowerCase(Locale.ROOT);
        if (firstKeyword.startsWith("set")) {
            return OperationType.SET;
        } else if (firstKeyword.startsWith("begin")) {
            return OperationType.BEGIN;
        } else if (firstKeyword.startsWith("insert")) {
            return OperationType.INSERT;
        } else if (firstKeyword.startsWith("delete")) {
            return OperationType.DELETE;
        } else if (firstKeyword.startsWith("update")) {
            return OperationType.UPDATE;
        } else if (firstKeyword.startsWith("select")) {
            return OperationType.SELECT;
        } else if (firstKeyword.startsWith("commit")) {
            return OperationType.COMMIT;
        } else {
            return OperationType.UNKNOWN;
        }
    }
}
