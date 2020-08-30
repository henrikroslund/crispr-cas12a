import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Stream;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;

    private static final String INPUT_FILENAME = "input.fasta";

    private static final String OUTPUT_FOLDER = "output/";
    private static final String OUTPUT_VALID_FILENAME = "valid.txt";
    private static final String OUTPUT_INVALID_FILENAME = "invalid.txt";
    private static final String OUTPUT_COMPLEMENT_PREFIX = "complement_";

    public static int VALID_SEQUENCE_COUNT = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        String folder = new Date().toString();
        File outputDir = new File(OUTPUT_FOLDER + folder);
        outputDir.mkdirs();

        String fileContent = getFileContent(INPUT_FILENAME);
        process(outputDir, fileContent, getFirstRow(INPUT_FILENAME));

        log.info(String.format("Found %,d valid sequences (including complement)", VALID_SEQUENCE_COUNT));

        printMemoryStat();
    }

    private static void process(File outputDir, String data, String outputFirstRow) throws Exception {
        BufferedWriter validWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_VALID_FILENAME, true));
        BufferedWriter validComplementWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_COMPLEMENT_PREFIX+OUTPUT_VALID_FILENAME, true));
        BufferedWriter invalidWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_INVALID_FILENAME, true));
        BufferedWriter invalidComplementWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + OUTPUT_COMPLEMENT_PREFIX+OUTPUT_INVALID_FILENAME, true));
        log.info("Writing results to: " + outputDir.getAbsolutePath());

        validWriter.append(outputFirstRow);
        validComplementWriter.append(outputFirstRow);
        invalidWriter.append(outputFirstRow);
        invalidComplementWriter.append(outputFirstRow);

        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            saveSequence(sequence, validWriter, invalidWriter);
            saveSequence(sequence.getComplement(), validComplementWriter, invalidComplementWriter);
        }

        // We have to close the writers or else it is possible that the write has not finished when program exists
        validWriter.close();
        validComplementWriter.close();
        invalidWriter.close();
        invalidComplementWriter.close();
    }

    private static void saveSequence(Sequence sequence, BufferedWriter validWriter, BufferedWriter invalidWriter) throws Exception {
        if(sequence.isValid()) {
            log.finest("Valid: " + sequence.toString());
            validWriter.append(sequence.toString() + "\n");
            VALID_SEQUENCE_COUNT++;
        } else {
            log.finest("Invalid: " + sequence.toString());
            invalidWriter.append(sequence.toString() + "\n");
        }
    }

    /**
     * Will return file content without any newlines
     */
    private static String getFileContent(String filename) throws Exception {
        Path filePath = Path.of("input.fasta");
        log.info("Input file is: " + filePath.toAbsolutePath());
        if (Files.notExists(filePath)) {
            throw new Exception("File does not exist: " + filePath.toAbsolutePath());
        }
        String fileContent = Files.readString(filePath).replaceAll("\n", "");
        log.finest(fileContent);
        log.info("Number of characters to process: " + fileContent.length());
        return fileContent;
    }

    private static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of("input.fasta");
        Stream<String> lines = Files.lines(filePath);
        return lines.findFirst().get() + "\n";
    }

    private static void printMemoryStat() {
        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory() / MEGABYTE_FACTOR;
        long free = rt.freeMemory() / MEGABYTE_FACTOR;

        long used = total - free;
        log.info("Total Memory: " + total + " MB, Used: " + used + ", Free: " + free);
    }

}
