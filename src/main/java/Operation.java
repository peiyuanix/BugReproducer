import lombok.Data;

@Data
public class Operation {
    private Integer seq;
    private String trxId;
    private String sql;
}
