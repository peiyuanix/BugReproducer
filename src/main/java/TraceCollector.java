import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TraceCollector {
    private static final Gson gson = new Gson();
    private static final List<Trace> traceList = new ArrayList<>();

    public static void collect(Trace trace) {
        traceList.add(trace);
        System.out.println(gson.toJson(trace));
    }
    public static void store(String dest) throws IOException {
        FileUtils.writeStringToFile(new File(dest), gson.toJson(traceList), StandardCharsets.UTF_8);
    }
}
