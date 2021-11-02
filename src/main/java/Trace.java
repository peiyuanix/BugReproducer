import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@Getter
public class Trace {
    private final String executorId;
    private final String sql;
    private final OperationType type;
    private Integer resultCount;
    private List<String> colNames;
    private List<List<String>> colValuesList;
    private String exception;


    public Trace(String executorId, String sql, OperationType type, int resultCount) {
        this.executorId = executorId;
        this.sql = sql;
        this.type = type;
        this.resultCount = resultCount;
    }

    public Trace(String executorId, String sql, OperationType type, Exception exception) {
        this.executorId = executorId;
        this.sql = sql;
        this.type = type;
        this.exception = ExceptionUtils.getMessage(exception);
    }

    public Trace(String executorId, String sql, OperationType type, List<String> colNames, List<List<String>> colValuesList) {
        this.executorId = executorId;
        this.sql = sql;
        this.type = type;
        this.colNames = colNames;
        this.colValuesList = colValuesList;
    }

    public Trace(String executorId, OperationType type) {
        this.executorId = executorId;
        this.type = type;
        this.sql = null;
    }
}
