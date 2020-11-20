package com.henrikroslund;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.BufferedReader;
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
    private String data;

    @Getter
    private final Collection<Sequence> sequences;

    private static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    private final boolean skipDuplicates;

    public Genome(boolean skipDuplicates, String outputFilename, String firstRow) {
        this.skipDuplicates = skipDuplicates;
        if(skipDuplicates) {
            sequences = Collections.synchronizedSet(new HashSet<>());
        } else {
            sequences = Collections.synchronizedList(new ArrayList<>());
        }
        this.outputFilename = outputFilename;
        this.firstRow = firstRow;
    }

    public Genome(File file, List<SequenceEvaluator> criteria, boolean skipDuplicates) throws Exception {
        this(skipDuplicates, file.getName().replace(".fasta", ""), Utils.getFirstRow(file.getAbsolutePath()));
        this.data = getFileContent(file.getAbsolutePath()).substring(firstRow.length());
        createSequences(criteria);
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
    private void createSequences(List<SequenceEvaluator> criteria) {
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
        writeSequences(outputFolder, "");
    }

    public void writeSequences(String outputFolder, String suffix) throws  Exception {
        saveSequence(sequences, outputFolder, outputFilename + suffix);
    }

    public static Genome loadGenome(File file) throws Exception {
        Path filePath = Path.of(file.getAbsolutePath());
        BufferedReader reader = Files.newBufferedReader(filePath);
        Genome genome = new Genome(true, file.getName(), reader.readLine());
        reader.lines().forEach(line -> {
            genome.sequences.add(Sequence.parseFromToString(line));
        });
        return genome;
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

    public boolean exists(Sequence sequence) {
        if(!skipDuplicates) {
            throw new IllegalArgumentException("Not supported");
        }
        return sequences.contains(sequence);
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
        return results;
    }

    public SequenceEvaluator hasAnyMatchToAnyEvaluator(List<SequenceEvaluator> evaluators) {
        SequenceEvaluator matchingEvaluator = null;
        for(Sequence sequence : sequences) {
            matchingEvaluator = SequenceEvaluator.matchAny(evaluators, sequence);
            if(matchingEvaluator != null) {
                return matchingEvaluator;
            }
        }
        return null;
    }

    public boolean removeAll(Collection<Sequence> sequences) {
        return this.sequences.removeAll(sequences);
    }

    public boolean removeMatchingSequences(SequenceEvaluator evaluator) {
        return sequences.removeIf(sequence -> evaluator.evaluate(sequence));
    }

    public int getTotalSequences() {
        return sequences.size();
    }
}
