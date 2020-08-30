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
        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i);
            addSequenceToList(sequence, validSequences, invalidSequences);
            Sequence complementSequence = sequence.getComplement();
            addSequenceToList(complementSequence, validComplementSequences, invalidComplementSequences);
        }
    }

    private void addSequenceToList(Sequence sequence, List<Sequence> valid, List<Sequence> invalid) {
        if (sequence.isValid()) {
            valid.add(sequence);
        } else if(shouldWriteInvalid) {
            invalid.add(sequence);
        }
    }

    public void saveSequences() throws  Exception {
        saveSequence(validSequences, Main.OUTPUT_FOLDER, outputFilename);
        saveSequence(validComplementSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX);
        saveSequence(resultingSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_RESULT_SUFFIX);
        saveSequence(resultingComplementSequences, Main.OUTPUT_FOLDER, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX + Main.OUTPUT_RESULT_SUFFIX);
        if(shouldWriteInvalid) {
            saveSequence(invalidSequences, Main.OUTPUT_FOLDER_INVALID, outputFilename + Main.OUTPUT_INVALID_SUFFIX);
            saveSequence(invalidComplementSequences, Main.OUTPUT_FOLDER_INVALID, outputFilename + Main.OUTPUT_COMPLEMENT_SUFFIX + Main.OUTPUT_INVALID_SUFFIX);
        }
    }

    private void saveSequence(List<Sequence> sequences, String folder, String filename) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Main.OUTPUT_FOLDER + filename, true));
        writer.append(firstRow);
        for(Sequence sequence : sequences) {
            writer.append(sequence.toString() + "\n");
        }
        log.info(String.format("Wrote %,d to \"" + filename+"\"", sequences.size()));
        writer.close();
    }

    public void createResult(List<Genome> genomes) {
        for(Genome genome : genomes) {
            if(genome != this) {
                log.info(this.getOutputFilename() + " processing genome: " + genome.getOutputFilename());
                for(Sequence sequence : validSequences) {
                    sequence.processMatchesInOtherGenome(genome);
                }
                for(Sequence sequence : validComplementSequences) {
                    sequence.processMatchesInOtherGenome(genome);
                }
            }
        }
        for(Sequence sequence : validSequences) {
            if(!sequence.shouldBeFilteredOut()) {
                resultingSequences.add(sequence);
            }
        }
        for(Sequence sequence : validComplementSequences) {
            if(!sequence.shouldBeFilteredOut()) {
                resultingComplementSequences.add(sequence);
            }
        }
    }
}
