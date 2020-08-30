import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Utils {

    public static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of(filename);
        Stream<String> lines = Files.lines(filePath);
        return lines.findFirst().get() + "\n";
    }
}
