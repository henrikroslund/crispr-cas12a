package com.henrikroslund;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVReader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.henrikroslund.Utils.*;

@Log
public class Genome {

    @Getter
    private final String filename;
    @Getter(AccessLevel.PROTECTED)
    private final String firstRow;

    @Getter
    private final Collection<Sequence> sequences;

    private boolean includeAllChromosomes = false;

    private final boolean skipDuplicates;
    private String absoluteFilePath;

    private static final int INITIAL_COLLECTION_CAPACITY = 75000;
    public static final String GENOME_FILE_ENDING = ".genome";
    private static String CSV_FILE_ENDING = ".csv";

    public Genome(boolean skipDuplicates, String filename, String firstRow) {
        this.skipDuplicates = skipDuplicates;
        if(skipDuplicates) {
            sequences = Collections.synchronizedSet(new HashSet<>(INITIAL_COLLECTION_CAPACITY));
        } else {
            sequences = Collections.synchronizedList(new ArrayList<>(INITIAL_COLLECTION_CAPACITY));
        }
        this.filename = filename.replace(FASTA_FILE_ENDING, "");
        this.firstRow = firstRow;
    }

    public Genome(File file, List<SequenceEvaluator> criteria, boolean skipDuplicates, boolean includeAllChromosomes) throws Exception {
        this(skipDuplicates, file.getName(), Utils.getFirstRow(file.getAbsolutePath()));
        this.includeAllChromosomes = includeAllChromosomes;
        this.absoluteFilePath = file.getAbsolutePath();
        if(absoluteFilePath.endsWith(FASTA_FILE_ENDING)) {
            if(includeAllChromosomes && isChromosomeFile(absoluteFilePath)) {
                if(isPrimaryChromosomeFile(absoluteFilePath)) {
                    ArrayList<String> chromosomeFiles = getChromosomeFiles(absoluteFilePath);
                    for(String chromosomeFile : chromosomeFiles) {
                        File genomeFile = new File(chromosomeFile);
                        if(genomeFile.exists()) {
                            log.info("Adding file " + genomeFile.getName());
                            String firstRow = Utils.getFirstRow(genomeFile.getAbsolutePath());
                            String genomeFileData = getFileContent(chromosomeFile).substring(firstRow.length()-1);
                            createSequences(criteria, genomeFileData);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Tried to create genome that includes all chromosomes but which is not a primary chromosome: " + file.getName());
                }
            } else {
                String data = getFileContent(absoluteFilePath).substring(firstRow.length()-1);
                createSequences(criteria, data);
            }
        } else if(absoluteFilePath.endsWith(GENOME_FILE_ENDING)) {
            BufferedReader reader = Files.newBufferedReader(Path.of(absoluteFilePath));
            reader.readLine();
            reader.lines().forEach(line -> {
                Sequence sequence = Sequence.parseFromToString(line);
                if(shouldAdd(criteria, sequence)) {
                    sequences.add(sequence);
                }
            });
        } else if(absoluteFilePath.endsWith(CSV_FILE_ENDING)) {
            CSVReader csvReader = new CSVReader(new FileReader(absoluteFilePath));
            String[] values;
            // First row is just the headers so we skip that one
            values = csvReader.readNext();
            while ((values = csvReader.readNext()) != null) {
                if(values.length != 3) {
                    throw new Exception("Expected exactly 3 columns but found " + values.length + " in file: " + absoluteFilePath);
                }
                if(values[2].compareTo("+") != 0 && values[2].compareTo("-") != 0) {
                    throw new Exception("Expected a + or - in column 3 but got: " + values[2]);
                }
                boolean isComplement = values[2].compareTo("-") == 0;
                Sequence sequence = new Sequence(values[1], 0, values[0], isComplement);
                if(shouldAdd(criteria, sequence)) {
                    sequences.add(sequence);
                }
            }
        } else {
            throw new Exception("Unknown file ending for file" + absoluteFilePath);
        }
    }

    @SneakyThrows
    public void saveSurroundingSequences(Sequence sequence, String outputFolder, String outputFilename) {
        if(includeAllChromosomes) {
            throw new Exception("Cannot save surrounding sequences when all chromosomes have been combines into one genome");
        }
        String firstRow = Utils.getFirstRow(absoluteFilePath);
        String genomeFileData = getFileContent(absoluteFilePath).substring(firstRow.length()-1);
        int nbrBeforeAndAfter = 2000;
        int startPosition = sequence.getStartIndex() - nbrBeforeAndAfter;
        if(startPosition < 0) {
            startPosition = 0;
        }
        int endPosition = sequence.getStartIndex() + nbrBeforeAndAfter;
        if(endPosition >= genomeFileData.length()) {
            endPosition = genomeFileData.length()-1;
        }
        String surroundingSequences = genomeFileData.substring(startPosition, endPosition);
        File outputFile = new File(outputFolder + "/" + outputFilename);
        if(outputFile.exists()) {
            throw new Exception("Did not expect file to already exists: " + outputFile.getAbsolutePath());
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
        writer.append(firstRow);
        writer.append(surroundingSequences);
        writer.close();
    }

    /**
     * Will return file content without any newlines
     */
    private static String getFileContent(String filename) throws IOException {
        Path filePath = Path.of(filename);
        log.info("Reading file: " + filePath.toAbsolutePath());
        if (Files.notExists(filePath)) {
            throw new IOException("File does not exist: " + filePath.toAbsolutePath());
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

        String genomeName = getStringWithoutWhitespaces(filename);
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
                + " ) sequences for " + filename);
    }

    private int calculatePotentialSequences(String sequenceData) {
        return (sequenceData.length() - (Sequence.RAW_LENGTH-1)) * 2;
    }

    private boolean shouldAdd(List<SequenceEvaluator> criteria, Sequence sequence) {
        return SequenceEvaluator.matchAll(criteria, sequence);
    }

    public void writeSequences(String outputFolder, String filename) throws Exception {
        saveSequence(sequences, outputFolder, filename + GENOME_FILE_ENDING);
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
        return getSequencesMatchingAnyEvaluator(Collections.singletonList(evaluator));
    }
    public List<Sequence> getSequencesMatchingAnyEvaluator(List<SequenceEvaluator> evaluators) {
        List<Sequence> results = Collections.synchronizedList(new ArrayList<>());
        sequences.forEach(sequence -> {
            if(SequenceEvaluator.matchAny(evaluators, sequence) != null) {
                results.add(sequence);
            }
        });
        return results;
    }

    public SequenceEvaluator hasAnyMatchToAnyEvaluator(List<SequenceEvaluator> evaluators) {
        SequenceEvaluator matchingEvaluator;
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

    public int getTotalSequences() {
        return sequences.size();
    }

    public void addAll(Collection<Sequence> sequences) {
        this.sequences.addAll(sequences);
    }
}
