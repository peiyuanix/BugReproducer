import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

@Data
public class OperationCollection {
    private String initUrl;
    private String connUrl;
    private String database;
    private String user;
    private String password;
    private boolean create;
    private boolean load;
    private List<String> createList;
    private List<String> loadList;
    private List<Operation> operationList;

    public static OperationCollection parse(String src) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(OperationCollection.class));
        return yaml.load(new FileReader(src));
    }
}
