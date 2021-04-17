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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.henrikroslund.Utils.*;

@Log
public class Genome {

    @Getter
    private final String outputFilename;
    private final String firstRow;
    private String data;

    @Getter
    private final Collection<Sequence> sequences;

    private final boolean skipDuplicates;

    private static final int INITIAL_COLLECTION_CAPACITY = 75000;

    public Genome(boolean skipDuplicates, String outputFilename, String firstRow) {
        this.skipDuplicates = skipDuplicates;
        if(skipDuplicates) {
            sequences = Collections.synchronizedSet(new HashSet<>(INITIAL_COLLECTION_CAPACITY));
        } else {
            sequences = Collections.synchronizedList(new ArrayList<>(INITIAL_COLLECTION_CAPACITY));
        }
        this.outputFilename = outputFilename;
        this.firstRow = firstRow;
    }

    public Genome(File file, List<SequenceEvaluator> criteria, boolean skipDuplicates, boolean includeAllChromosomes) throws Exception {
        this(skipDuplicates, file.getName().replace(".fasta", ""), Utils.getFirstRow(file.getAbsolutePath()));

        if(includeAllChromosomes && isChromosomeFile(file.getAbsolutePath())) {
            if(isPrimaryChromosomeFile(file.getAbsolutePath())) {
                ArrayList<String> chromosomeFiles = getChromosomeFiles(file.getAbsolutePath());
                for(String chromosomeFile : chromosomeFiles) {
                    File genomeFile = new File(chromosomeFile);
                    if(genomeFile.exists()) {
                        log.info("Adding file " + genomeFile.getName());
                        String firstRow = Utils.getFirstRow(file.getAbsolutePath());
                        String genomeFileData = getFileContent(chromosomeFile).substring(firstRow.length());
                        if(isPrimaryChromosomeFile(genomeFile.getAbsolutePath())) {
                            this.data = genomeFileData;
                        }
                        createSequences(criteria, genomeFileData);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tried to create genome that includes all chromosomes but which is not a primary choromosome: " + file.getName());
            }
        } else {
            this.data = getFileContent(file.getAbsolutePath()).substring(firstRow.length()-1);
            createSequences(criteria, this.data);
        }
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
    protected void createSequences(List<SequenceEvaluator> criteria, String sequenceData) {
        List<Integer> range = IntStream.rangeClosed(0, sequenceData.length() - (Sequence.RAW_LENGTH-1) - 1)
                .boxed().collect(Collectors.toList());

        String genomeName = getStringWithoutWhitespaces(outputFilename);
        range.parallelStream().forEach(i -> {
            Sequence sequence = new Sequence(sequenceData.substring(i, i+Sequence.RAW_LENGTH), i, genomeName);

            if(shouldAdd(criteria, sequence)) {
                sequence.getRawHash();
                sequences.add(sequence);
            }

            Sequence complement = sequence.getComplement();
            if(shouldAdd(criteria, complement)) {
                complement.getRawHash();
                sequences.add(complement);
            }
        });
        log.info("Finished creating " + getTotalSequences() + " ( " + calculatePotentialSequences(sequenceData)
                + " ) sequences for " + outputFilename);
    }

    private int calculatePotentialSequences(String sequenceData) {
        return (sequenceData.length() - (Sequence.RAW_LENGTH-1)) * 2;
    }

    private boolean shouldAdd(List<SequenceEvaluator> criteria, Sequence sequence) {
        return SequenceEvaluator.matchAll(criteria, sequence);
    }

    public void writeSequences(String outputFolder) throws  Exception {
        writeSequences(outputFolder, "");
    }

    public void writeSequences(String outputFolder, String suffix) throws Exception {
        writeSequences(outputFolder, outputFilename, suffix);
    }

    public void writeSequences(String outputFolder, String filename, String suffix) throws Exception {
        saveSequence(sequences, outputFolder, filename + suffix);
    }

    public static Genome loadGenome(File file) throws Exception {
        Path filePath = Path.of(file.getAbsolutePath());
        BufferedReader reader = Files.newBufferedReader(filePath);
        Genome genome = new Genome(true, file.getName(), reader.readLine()+ "\n");
        reader.lines().forEach(line -> {
            genome.sequences.add(Sequence.parseFromToString(line));
        });
        return genome;
    }

    private void saveSequence(Collection<Sequence> sequences, String outputFolder, String filename) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "/" + filename, true));
        writer.append(firstRow);

        for(Sequence sequence : sequences) {
            writer.append(sequence.serialize()).append("\n");
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
    public List<Sequence> getSequencesMatchingAnyEvaluator(SequenceEvaluator evaluator) {
        return getSequencesMatchingAnyEvaluator(Arrays.asList(evaluator));
    }
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

    public Sequence getSequenceMatchingAllEvaluators(List<SequenceEvaluator> evaluators) {
        for(Sequence sequence : sequences) {
            if(SequenceEvaluator.matchAll(evaluators, sequence)) {
                return sequence;
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

    public void addAll(Collection<Sequence> sequences) {
        this.sequences.addAll(sequences);
    }
}
