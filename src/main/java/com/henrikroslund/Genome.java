package com.henrikroslund;

import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log
public class Genome {

    @Getter
    private final String outputFilename;
    private final String firstRow;
    private final String data;

    @Getter
    private final List<Sequence> sequences = Collections.synchronizedList(new ArrayList<>());
    @Getter
    private final List<Sequence> complementSequences = Collections.synchronizedList(new ArrayList<>());

    private static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    public Genome(File file, List<SequenceEvaluator> criteria, boolean skipDuplicates) throws Exception {
        this.outputFilename = file.getName().replace(".fasta", "");
        this.firstRow = Utils.getFirstRow(file.getAbsolutePath());
        this.data = getFileContent(file.getAbsolutePath()).substring(firstRow.length());
        createSequences(criteria, skipDuplicates);
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
     * Will create the sequences and store them in the appropriate list.
     * @param criteria a list of filters to determine if sequence should be added to gnome
     */
    private void createSequences(List<SequenceEvaluator> criteria, boolean skipDuplicates) {
        log.info("Will process " + data.length() + " potential sequences");
        int stepsPerPercent = data.length() / 100;
        int skipCount = 0;
        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i, outputFilename);
            if(shouldAdd(criteria, sequence, skipDuplicates)) {
                sequences.add(sequence);
            } else {
                skipCount++;
            }
            Sequence complement = sequence.getComplement();
            if(shouldAdd(criteria, complement, skipDuplicates)) {
                complementSequences.add(complement);
            } else {
                skipCount++;
            }
            if(i % stepsPerPercent == 0) {
                log.info("Creating Sequences " + i/stepsPerPercent + "%");
            }
        }
        log.info("Finished creating " + getTotalSequences() + " sequences for " + outputFilename + " with " + skipCount + " skipped ");
    }

    private boolean shouldAdd(List<SequenceEvaluator> criteria, Sequence sequence, boolean skipDuplicates) {
        if(SequenceEvaluator.matchAll(criteria, sequence)) {
            if(skipDuplicates) {
                return getSequencesMatchingAnyEvaluator(Collections.singletonList(new IdenticalEvaluator(sequence))).isEmpty();
            }
            return true;
        }
        return false;
    }

    public void writeSequences(String outputFolder) throws  Exception {
        saveSequence(sequences, outputFolder, outputFilename);
        saveSequence(complementSequences, outputFolder, outputFilename + OUTPUT_COMPLEMENT_SUFFIX);
    }

    private void saveSequence(List<Sequence> sequences, String outputFolder, String filename) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + filename, true));
        writer.append(firstRow);

        for(Sequence sequence : sequences) {
            writer.append(sequence.toString() + "\n");
        }
        log.info(String.format("Wrote %,d to \"" + filename+"\"", sequences.size()));
        writer.close();
    }

    /**
     * Will return sequences that match any of the evaluators
     */
    public List<Sequence> getSequencesMatchingAnyEvaluator(List<SequenceEvaluator> evaluators) {
        List<Sequence> results = Collections.synchronizedList(new ArrayList<>());
        sequences.parallelStream().forEach(sequence -> {
            if(SequenceEvaluator.matchAny(evaluators, sequence))
                results.add(sequence);
        });
        complementSequences.parallelStream().forEach(sequence -> {
            if(SequenceEvaluator.matchAny(evaluators, sequence))
                results.add(sequence);
        });
        return results;
    }

    public boolean removeMatchingSequences(SequenceEvaluator evaluator) {
        boolean didRemoveSequence = sequences.removeIf(sequence -> evaluator.evaluate(sequence));
        boolean didRemoveComplement = complementSequences.removeIf(sequence -> evaluator.evaluate(sequence));
        return didRemoveSequence || didRemoveComplement;
    }

    public int getTotalSequences() {
        return sequences.size() + complementSequences.size();
    }
}
