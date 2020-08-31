import lombok.Getter;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Log
public class Genome {

    @Getter
    private final String outputFilename;
    private final String firstRow;
    private final String data;

    @Getter
    private final List<Sequence> validSequences = new ArrayList<>();
    private final List<Sequence> invalidSequences = new ArrayList<>();

    @Getter
    private final List<Sequence> validComplementSequences = new ArrayList<>();
    private final List<Sequence> invalidComplementSequences = new ArrayList<>();

    private final List<Sequence> resultingSequences = new ArrayList<>();
    private final List<Sequence> resultingComplementSequences = new ArrayList<>();

    // This contains the list of other genomes that was processed to get the result list of this genome's sequences
    private List<Genome> processedGenomes = null;

    private final boolean shouldWriteInvalid;

    public Genome(File file, boolean shouldWriteInvalid) throws Exception {
        this.shouldWriteInvalid = shouldWriteInvalid;
        this.outputFilename = file.getName().replace(".fasta", "");
        this.firstRow = Utils.getFirstRow(file.getAbsolutePath());
        this.data = getFileContent(file.getAbsolutePath());
        createSequences();
    }

    /**
     * Will return file content without any newlines
     */
    private static String getFileContent(String filename) throws Exception {
        Path filePath = Path.of(filename);
        log.info("Reading file: " + filePath.toAbsolutePath());
        if (Files.notExists(filePath)) {
            throw new Exception("File does not exist: " + filePath.toAbsolutePath());
        }
        String fileContent = Files.readString(filePath).replaceAll("\n", "");
        log.finest(fileContent);
        log.finest("Number of characters to process: " + fileContent.length());
        return fileContent;
    }

    /**
     * Will create the sequences and store them in the appropriate list
     * @throws Exception
     */
    private void createSequences() throws Exception {
        log.info("Will process " + data.length() + " potential sequences");
        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            addSequenceToList(sequence, validSequences, invalidSequences);
            Sequence complementSequence = sequence.getComplement();
            addSequenceToList(complementSequence, validComplementSequences, invalidComplementSequences);
        }
        log.info("Finished processing sequences");
    }

    private void addSequenceToList(Sequence sequence, List<Sequence> valid, List<Sequence> invalid) {
        if (sequence.isValid()) {
            valid.add(sequence);
        } else if(shouldWriteInvalid) {
            invalid.add(sequence);
        }
    }

    public void saveSequences() throws  Exception {
        saveSequence(validSequences, Main.OUTPUT_FOLDER, outputFilename, false);
        saveSequence(resultingSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_RESULT_SUFFIX, true);
        saveSequence(validComplementSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX, false);
        saveSequence(resultingComplementSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX + Main.OUTPUT_RESULT_SUFFIX, true);
        if(shouldWriteInvalid) {
            saveSequence(invalidSequences, Main.OUTPUT_FOLDER_INVALID, outputFilename + Main.OUTPUT_INVALID_SUFFIX, false);
            saveSequence(invalidComplementSequences, Main.OUTPUT_FOLDER_INVALID, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX + Main.OUTPUT_INVALID_SUFFIX, false);
        }
    }

    private void saveSequence(List<Sequence> sequences, String folder, String filename, boolean writeProcessedGenomes) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FOLDER + filename, true));
        writer.append(firstRow);
        if(writeProcessedGenomes) {
            writer.append(getProcessedGenomesString());
            writer.append(Sequence.getRulesApplied());
        }
        for(Sequence sequence : sequences) {
            writer.append(sequence.toString() + "\n");
        }
        log.info(String.format("Wrote %,d to \"" + filename+"\"", sequences.size()));
        writer.close();
    }

    private String getProcessedGenomesString() {
        String result = "*********** The following Genomes were used for filtering results ***********\n";
        for(Genome genome : processedGenomes) {
            result += genome.getOutputFilename() + "\n";
        }
        result += "*****************************************************************************\n";
        return result;
    }

    public void processGenomes(List<Genome> genomes) throws Exception {
        if(processedGenomes != null) {
            throw new Exception("This Genome has already processed other Genomes so something is wrong");
        }
        processedGenomes = new ArrayList<>(genomes);
        processedGenomes.remove(this);
        log.info("Will process this genome " + outputFilename + " with " + processedGenomes.size() + " other genomes");
        for(Sequence sequence : validSequences) {
            sequence.processMatchesInOtherGenomes(processedGenomes);
        }
        for(Sequence sequence : validComplementSequences) {
            sequence.processMatchesInOtherGenomes(processedGenomes);
        }
        for(Sequence sequence : validSequences) {
            if(!sequence.shouldBeFilteredOutBasedOnMatchesInOtherGenomes()) {
                resultingSequences.add(sequence);
            }
        }
        for(Sequence sequence : validComplementSequences) {
            if(!sequence.shouldBeFilteredOutBasedOnMatchesInOtherGenomes()) {
                resultingComplementSequences.add(sequence);
            }
        }
        log.info("Finished Genomes processing");
    }
}
