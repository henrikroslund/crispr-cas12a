package com.henrikroslund;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Log
public class Genome {

    @Getter
    private final String outputFilename;
    private final String firstRow;
    private final String data;

    @Getter
    private final Collection<Sequence> sequences;
    @Getter
    private final Collection<Sequence> complementSequences;

    private static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    public Genome(File file, List<SequenceEvaluator> criteria, boolean skipDuplicates) throws Exception {
        if(skipDuplicates) {
            sequences = Collections.synchronizedSet(new HashSet<>());
            complementSequences = Collections.synchronizedSet(new HashSet<>());
        } else {
            sequences = Collections.synchronizedList(new ArrayList<>());
            complementSequences = Collections.synchronizedList(new ArrayList<>());
        }

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

            int beforeCount = sequences.size();
            if(shouldAdd(criteria, sequence)) {
                sequences.add(sequence);
            }
            if(beforeCount == sequences.size()) {
                skipCount++;
            }

            beforeCount = complementSequences.size();
            Sequence complement = sequence.getComplement();
            if(shouldAdd(criteria, complement)) {
                complementSequences.add(complement);
            }
            if(beforeCount == complementSequences.size()) {
                skipCount++;
            }


            if(i % stepsPerPercent == 0) {
                log.info("Creating Sequences " + i/stepsPerPercent + "%");
            }
        }
        log.info("Finished creating " + getTotalSequences() + " sequences for " + outputFilename + " with " + skipCount + " skipped ");
    }

    private boolean shouldAdd(List<SequenceEvaluator> criteria, Sequence sequence) {
        if(SequenceEvaluator.matchAll(criteria, sequence)) {
            return true;
        }
        return false;
    }

    public void writeSequences(String outputFolder) throws  Exception {
        saveSequence(sequences, outputFolder, outputFilename);
        saveSequence(complementSequences, outputFolder, outputFilename + OUTPUT_COMPLEMENT_SUFFIX);
    }

    private void saveSequence(Collection<Sequence> sequences, String outputFolder, String filename) throws Exception {
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
        sequences.stream().forEach(sequence -> {
            if(SequenceEvaluator.matchAny(evaluators, sequence) != null) {
                results.add(sequence);
            }
        });
        complementSequences.stream().forEach(sequence -> {
            if(SequenceEvaluator.matchAny(evaluators, sequence) != null) {
                results.add(sequence);
            }
        });
        return results;
    }

    public SequenceEvaluator hasAnyMatchToAnyEvaluator(List<SequenceEvaluator> evaluators) {
        SequenceEvaluator matchingEvaluator = null;
        for(Sequence sequence : sequences) {
            matchingEvaluator = SequenceEvaluator.matchAny(evaluators, sequence);
            if(matchingEvaluator != null) {
                matchingEvaluator.setMatch(sequence);
                return matchingEvaluator;
            }
        }
        for(Sequence sequence : complementSequences) {
            matchingEvaluator = SequenceEvaluator.matchAny(evaluators, sequence);
            if(matchingEvaluator != null) {
                matchingEvaluator.setMatch(sequence);
                return matchingEvaluator;
            }
        }
        return null;
    }

    public boolean removeAll(List<Sequence> sequences) {
        boolean removed = this.sequences.removeAll(sequences);
        boolean removedComplement = complementSequences.removeAll(sequences);
        return removed || removedComplement;
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
