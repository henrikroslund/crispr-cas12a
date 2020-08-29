import lombok.extern.java.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    private static final String OUTPUT_FOLDER = "output";

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        String fileContent = getFileContent("input.fasta");

        createFolderIfNotExists(".", OUTPUT_FOLDER);
        String folder = new Date().toString();
        createFolderIfNotExists(OUTPUT_FOLDER, folder);

        for(int i = 0; i < fileContent.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(fileContent.substring(i, i+Sequence.RAW_LENGTH), i);
            if(sequence.isValid()) {
                log.finest("Valid: " + sequence.getRaw());
            } else {
                log.finest("Invalid: " + sequence.getRaw());
            }
        }
        printMemoryStat();
    }

    private static void createFolderIfNotExists(String parentDir, String dirname) {
        File dir = new File(parentDir, dirname);
        if(!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Will return file content without any newlines
     */
    private static String getFileContent(String filename) throws Exception {
        Path filePath = Path.of("input.fasta");
        log.info("Filepath is: " + filePath.toAbsolutePath());
        if (Files.notExists(filePath)) {
            throw new Exception("File does not exist: " + filePath.toAbsolutePath());
        }
        String fileContent = Files.readString(filePath).replaceAll("\n", "");
        log.info(fileContent);
        log.info(String.valueOf(fileContent.length()));
        return fileContent;
    }

    private static void printMemoryStat() {
        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory() / MEGABYTE_FACTOR;
        long free = rt.freeMemory() / MEGABYTE_FACTOR;

        long used = total - free;
        log.info("Total: " + total + ", Used: " + used + ", Free: " + free);
    }

}
