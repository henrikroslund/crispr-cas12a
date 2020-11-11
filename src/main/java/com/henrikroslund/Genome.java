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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log
public class Genome {

    @Getter
    private final String outputFilename;
    private final String firstRow;
    private final String data;

    private final List<Sequence> sequences = new ArrayList<>();
    private final List<Sequence> complementSequences = new ArrayList<>();

    private static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    public Genome(File file, List<SequenceEvaluator> criteria) throws Exception {
        this.outputFilename = file.getName().replace(".fasta", "");
        this.firstRow = Utils.getFirstRow(file.getAbsolutePath());
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
        for(int i = 0; i < data.length() - (Sequence.RAW_LENGTH-1); i++) {
            Sequence sequence = new Sequence(data.substring(i, i+Sequence.RAW_LENGTH), i, outputFilename);
            if(SequenceEvaluator.matchAll(criteria, sequence)) {
                sequences.add(sequence);
            }
            Sequence complement = sequence.getComplement();
            if(SequenceEvaluator.matchAll(criteria, complement)) {
                complementSequences.add(complement);
            }
            if(i % stepsPerPercent == 0) {
                log.info("Finished " + i/stepsPerPercent + "%");
            }
        }
        log.info("Finished processing sequences");
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

    public List<Sequence> getMatchingSequences(SequenceEvaluator evaluator) {
        List<Sequence> results = Collections.synchronizedList(new ArrayList<>());
        sequences.parallelStream().forEach(sequence -> {
            if(evaluator.evaluate(sequence)) {
                results.add(sequence);
            }
        });
        complementSequences.parallelStream().forEach(sequence -> {
            if(evaluator.evaluate(sequence)) {
                results.add(sequence);
            }
        });
        return results;
    }
}
