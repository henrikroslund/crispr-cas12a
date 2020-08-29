import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;

    private static final String INPUT_FILENAME = "input.fasta";

    private static final String OUTPUT_FOLDER = "output/";
    private static final String OUTPUT_VALID_FILENAME = "valid.txt";
    private static final String OUTPUT_INVALID_FILENAME = "invalid.txt";

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        String folder = new Date().toString();
        File outputDir = new File(OUTPUT_FOLDER + folder);
        outputDir.mkdirs();

        String fileContent = getFileContent(INPUT_FILENAME);
        process(outputDir, fileContent);

        printMemoryStat();
    }

    private static void process(File outputDir, String data) throws Exception {
        BufferedWriter validWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_VALID_FILENAME, true));
        BufferedWriter invalidWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_INVALID_FILENAME, true));

        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            if(sequence.isValid()) {
                log.finest("Valid: " + sequence.toString());
                validWriter.append(sequence.toString() + "\n");
            } else {
                log.finest("Invalid: " + sequence.toString());
                invalidWriter.append(sequence.toString() + "\n");
            }
        }
        validWriter.close();
        invalidWriter.close();
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
