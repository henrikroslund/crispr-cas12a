import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;

    private static final String INPUT_FOLDER = "input/";
    private static final String INPUT_FILENAME = "input.fasta";
    private static final String INPUT_COMPARISON_FOLDER = INPUT_FOLDER + "comparison/";

    private static final String OUTPUT_FOLDER = "output/";
    private static final String OUTPUT_FOLDER_INVALID = "invalid/";
    private static final String OUTPUT_INVALID_SUFFIX = "_invalid";
    private static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    public static int VALID_SEQUENCE_COUNT = 0;

    private static final boolean WRITE_INVALID = false;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        String folder = new Date().toString();
        File outputDir = new File(OUTPUT_FOLDER + folder);
        outputDir.mkdirs();
        File outputInvalidDir = new File(OUTPUT_FOLDER + folder + "/" + OUTPUT_FOLDER_INVALID);
        outputInvalidDir.mkdirs();

        String comparisonFilesOutput = "*********** The following Genomes were used for filtering results ***********\n";

        File[] comparisonFiles = getFilesInFolder(INPUT_COMPARISON_FOLDER);
        ArrayList<Sequence> comparisonSequences = new ArrayList<>();
        for(File comparisonFile : comparisonFiles) {
            String compContent = getFileContent(comparisonFile.getAbsolutePath());
            addValidComparisonSequences(compContent, comparisonSequences);
            comparisonFilesOutput += getFirstRow(comparisonFile.getAbsolutePath());
        }
        comparisonFilesOutput += "*****************************************************************************\n";
        log.info("Found " + comparisonSequences.size() + " sequences to compare with");

        String fileContent = getFileContent(INPUT_FOLDER + INPUT_FILENAME);
        process(outputDir, outputInvalidDir, fileContent, getFirstRow(INPUT_FOLDER + INPUT_FILENAME), comparisonFilesOutput, comparisonSequences);

        log.info(String.format("Found %,d valid sequences (including complement)", VALID_SEQUENCE_COUNT));

        printMemoryStat();
    }

    private static File[] getFilesInFolder(String folder) {
        File directoryPath = new File(folder);
        return directoryPath.listFiles();
    }

    private static void addValidComparisonSequences(String data, ArrayList<Sequence> list) throws Exception {
        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            Sequence sequenceComplement = sequence.getComplement();
            if(sequence.isValid()) {
                list.add(sequence);
            }
            if(sequenceComplement.isValid()) {
                list.add(sequenceComplement);
            }
        }
    }

    private static void process(File outputDir, File outputInvalidDir, String data, String outputFirstRow, String comparisonFirstRows, List<Sequence> comparisonSequences) throws Exception {
        BufferedWriter validWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + outputFirstRow, true));
        BufferedWriter resultWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + "result_" + outputFirstRow, true));
        BufferedWriter resultComplementWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + "result_" + outputFirstRow + OUTPUT_COMPLEMENT_SUFFIX, true));
        BufferedWriter validComplementWriter = new BufferedWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + outputFirstRow + OUTPUT_COMPLEMENT_SUFFIX, true));
        BufferedWriter invalidWriter = new BufferedWriter(new FileWriter(outputInvalidDir.getAbsolutePath() + "/" + outputFirstRow + OUTPUT_INVALID_SUFFIX, true));
        BufferedWriter invalidComplementWriter = new BufferedWriter(new FileWriter(outputInvalidDir.getAbsolutePath() + "/" + outputFirstRow + OUTPUT_INVALID_SUFFIX + OUTPUT_COMPLEMENT_SUFFIX, true));
        log.info("Writing results to: " + outputDir.getAbsolutePath());

        resultComplementWriter.append(outputFirstRow);
        resultComplementWriter.append(comparisonFirstRows);
        resultWriter.append(outputFirstRow);
        resultWriter.append(comparisonFirstRows);
        validWriter.append(outputFirstRow);
        validComplementWriter.append(outputFirstRow);
        invalidWriter.append(outputFirstRow);
        invalidComplementWriter.append(outputFirstRow);

        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            saveSequence(sequence, validWriter, invalidWriter, resultWriter, comparisonSequences);
            saveSequence(sequence.getComplement(), validComplementWriter, invalidComplementWriter, resultComplementWriter, comparisonSequences);
        }

        // We have to close the writers or else it is possible that the write has not finished when program exists
        resultWriter.close();
        resultComplementWriter.close();
        validWriter.close();
        validComplementWriter.close();
        invalidWriter.close();
        invalidComplementWriter.close();
    }

    private static void saveSequence(Sequence sequence, BufferedWriter validWriter, BufferedWriter invalidWriter, BufferedWriter resultWriter, List<Sequence> comparisonSequences) throws Exception {
        if(sequence.isValid()) {
            log.finest("Valid: " + sequence.toString());
            validWriter.append(sequence.toString() + "\n");
            if(!sequence.isMatchWith(comparisonSequences)) {
                resultWriter.append(sequence.toString() + "\n");
                VALID_SEQUENCE_COUNT++;
            }
        } else {
            log.finest("Invalid: " + sequence.toString());
            if(WRITE_INVALID) {
                invalidWriter.append(sequence.toString() + "\n");
            }
        }
    }

    /**
     * Will return file content without any newlines
     */
    private static String getFileContent(String filename) throws Exception {
        Path filePath = Path.of(filename);
        log.info("Input file is: " + filePath.toAbsolutePath());
        if (Files.notExists(filePath)) {
            throw new Exception("File does not exist: " + filePath.toAbsolutePath());
        }
        String fileContent = Files.readString(filePath).replaceAll("\n", "");
        log.finest(fileContent);
        log.finest("Number of characters to process: " + fileContent.length());
        return fileContent;
    }

    private static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of(filename);
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
