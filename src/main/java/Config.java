import lombok.Data;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

@Data
public class Config {
    private String initUrl;
    private String connUrl;
    private String user;
    private String password;
    private boolean create;
    private boolean load;
    private List<String> createDatabaseList;
    private List<String> createTableList;
    private List<String> loadList;
    private List<Operation> operationList;

    public static Config parse(String src) throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        return yaml.load(new FileReader(src));
    }
}
