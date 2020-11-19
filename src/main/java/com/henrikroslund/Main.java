package com.henrikroslund;

import com.henrikroslund.evaluators.*;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.formats.JakeCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import com.henrikroslund.sequence.SequenceReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> memUsageHandle = scheduler.scheduleAtFixedRate(Main::printMemoryStat, 1, 5, TimeUnit.SECONDS);
    static long maxMemUsage = 0;

    static final boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        long start = new Date().getTime();
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        //runJake();
        //runPop();
        //runPopV2();
        //runPopV3();
        testReadWrite();

        memUsageHandle.cancel(false);
        scheduler.shutdown();
        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
    }

    private static void testReadWrite() throws Exception {
        String inputFolder = "input/pop/";
        String outputFolder = "output/" + new Date().toString() + " pop/";
        String outputInputFolder = outputFolder + "input/";
        // Create output folders
        new File(outputFolder).mkdirs();
        new File(outputInputFolder).mkdirs();
        Genome suis_ss2_1 = getPopSuis(inputFolder, outputInputFolder);
        log.info("Read suis genome with " + suis_ss2_1.getAllSequences().size() + " sequences");

        Genome genome = Genome.loadGenome(new File(outputInputFolder+suis_ss2_1.getOutputFilename()));
        Genome genome2 = Genome.loadGenome(new File(outputInputFolder+suis_ss2_1.getOutputFilename()));
        log.info("" + genome.getAllSequences().size());
    }

    private static Genome getPopSuis(String inputFolder, String outputInputFolder) throws Exception {
        // Read the original suis genome
        File suisGenomeFile = new File(inputFolder + "Suis strain SS2-1 sequence.fasta");
        FileUtils.copyFile(suisGenomeFile, new File(outputInputFolder+suisGenomeFile.getName()));
        Genome suis_ss2_1 = new Genome(suisGenomeFile, Arrays.asList(
                new CrisprPamEvaluator(), new NoTripletN1N20Evaluator(), new GCContentN1N20Evaluator()), true);
        suis_ss2_1.writeSequences(outputInputFolder);
        return suis_ss2_1;
    }

    private static void runPopV3() throws Exception {
        String inputFolder = "input/pop/";
        String outputFolder = "output/" + new Date().toString() + " pop/";
        String outputInputFolder = outputFolder + "input/";
        // Create output folders
        new File(outputFolder).mkdirs();
        new File(outputInputFolder).mkdirs();

        Genome suis_ss2_1 = getPopSuis(inputFolder, outputInputFolder);
        log.info("Read suis genome with " + suis_ss2_1.getAllSequences().size() + " sequences");

        List<File> suisFiles = getFilesInFolder(inputFolder+"suisGenomes/", ".fasta");
        List<Sequence> duplicates = getDuplicates(suis_ss2_1.getAllSequences(), suisFiles);
        suis_ss2_1.removeAll(duplicates);
    }

    private static List<Sequence> getDuplicates(Collection<Sequence> sequences, List<File> files) {
        Collection<Sequence> duplicates = Collections.synchronizedSet(new HashSet<>());
        files.stream().forEach(file -> {
            try {
                Date startTime = new Date();
                Genome genome = new Genome(file, Collections.EMPTY_LIST, true);
                AtomicInteger counter = new AtomicInteger(0);
                for(Sequence sequence: sequences) {
                    SequenceEvaluator evaluator = genome.hasAnyMatchToAnyEvaluator(Arrays.asList(new IdenticalEvaluator(sequence)));
                    if(evaluator != null) {
                        duplicates.add(sequence);
                        log.info("Number of duplicates: " + duplicates.size());
                    }
                    counter.incrementAndGet();
                    log.info("Counter: " + counter + " total: " + sequences.size());
                }
                log.info("Finished processing file in " + (new Date().getTime() - startTime.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        log.info("Found " + duplicates.size() + " duplicates");
        return new ArrayList<>(duplicates);
    }

    private static void runPopV2() throws Exception {
        String inputFolder = "input/pop/";
        String outputFolder = "output/" + new Date().toString() + " pop/";
        String outputInputFolder = outputFolder + "input/";
        // Create output folders
        new File(outputFolder).mkdirs();
        new File(outputInputFolder).mkdirs();

        // Read the original suis genome
        File suisGenomeFile = new File(inputFolder + "Suis strain SS2-1 sequence.fasta");
        FileUtils.copyFile(suisGenomeFile, new File(outputInputFolder+suisGenomeFile.getName()));
        Genome suis_ss2_1 = new Genome(suisGenomeFile, Arrays.asList(
                new CrisprPamEvaluator(), new NoTripletN1N20Evaluator(), new GCContentN1N20Evaluator()), true);
        suis_ss2_1.writeSequences(outputInputFolder);

        List<File> files = getFilesInFolder(inputFolder+"genomes/", ".fasta");
        log.info("Found " + files.size() + " genome files");

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + suis_ss2_1.getOutputFilename() + "_evaluations", true));
        BufferedWriter candidateWriter = new BufferedWriter(new FileWriter(outputFolder + suis_ss2_1.getOutputFilename() + "_candidates", true));

        List<Sequence> sequencesToBeRemoved = new ArrayList<>();
        for(File file : files) {
            sequencesToBeRemoved.clear();
            Genome genome = new Genome(file, Collections.EMPTY_LIST, true);
            for(Sequence sequence: suis_ss2_1.getAllSequences()) {
                SequenceEvaluator evaluator = genome.hasAnyMatchToAnyEvaluator(Arrays.asList(new IdenticalEvaluator(sequence)));
                if(evaluator != null) {
                    sequencesToBeRemoved.add(sequence);
                    writer.append(sequence.toString() + " removed because " + evaluator.toString() + "\n");
                }
            }
            suis_ss2_1.removeAll(sequencesToBeRemoved);
            log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        }

        log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        Collection<Sequence> suisSequences = suis_ss2_1.getAllSequences();
        int validCandidates = 0;
        for(Sequence suisSequence : suisSequences) {
            writer.append(suisSequence.toString() + " ");
            boolean shouldBeRemoved = false;

            for(File file : files) {
                Genome genome = new Genome(file, Collections.EMPTY_LIST, true);
                TypeEvaluator genomeEvaluator = null;

                for(Sequence genomeSequence : genome.getAllSequences()) {
                    TypeEvaluator evaluator = new TypeEvaluator(suisSequence);
                    if(evaluator.evaluate(genomeSequence)) {
                        genomeEvaluator = evaluator;
                    }
                    if(genomeEvaluator != null && genomeEvaluator.isDiscardType()) {
                        // The evaluator marked the suis sequence for removal so we stop searching
                        break;
                    }
                }

                if(genomeEvaluator == null || genomeEvaluator.getMatchType() == null) {
                    // Could not find a match in the genome, this is unexpected
                    throw new Exception("Could not determine match type for suis sequence: " + suisSequence.toString() + " in genome " + genome.getOutputFilename());
                } else if(genomeEvaluator.isDiscardType()) {
                    shouldBeRemoved = true;
                } else {
                    // There was a match so continue to next file
                }

                writer.append("\t" + genomeEvaluator.getMatchType().name() + " " + genomeEvaluator.getMatch().toString());
                log.info("Finished with file " + genome.getOutputFilename());
            }

            if(shouldBeRemoved) {
                suis_ss2_1.removeAll(Arrays.asList(suisSequence));
                log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
            } else {
                candidateWriter.append(suisSequence.toString() + "\n");
                validCandidates++;
                log.info("Wrote valid candidate number " + validCandidates);
            }
            writer.append("\n");
        }
        writer.close();
        suis_ss2_1.writeSequences(outputFolder);

    }

    private static void runPop() throws Exception {
        String inputFolder = "input/pop/";
        String outputFolder = "output/" + new Date().toString() + " pop/";
        String outputInputFolder = outputFolder + "input/";
        // Create output folders
        new File(outputFolder).mkdirs();
        new File(outputInputFolder).mkdirs();

        // Read the original suis genome
        File suisGenomeFile = new File(inputFolder + "Suis strain SS2-1 sequence.fasta");
        FileUtils.copyFile(suisGenomeFile, new File(outputInputFolder+suisGenomeFile.getName()));
        Genome suis_ss2_1 = new Genome(suisGenomeFile, Arrays.asList(
                new CrisprPamEvaluator(), new NoTripletN1N20Evaluator(), new GCContentN1N20Evaluator()), true);
        suis_ss2_1.writeSequences(outputInputFolder);
        Collection<Sequence> suisSequences = suis_ss2_1.getSequences();

        List<File> files = getFilesInFolder(inputFolder+"genomes/", ".fasta");
        log.info("Found " + files.size() + " genome files");
        int numberOfFilesProcessed = 0;

        for(File file: files) {
            Genome genome = new Genome(file, Collections.EMPTY_LIST, true);
            int countBefore = suis_ss2_1.getTotalSequences();
            log.info("Suis count remaining: " + countBefore);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "suis_sequences_removed_by_"+genome.getOutputFilename(), true));
            writer.append("Sequences in this file are suis ss2-1 which were removed by matches in genome " + genome.getOutputFilename() + "\n");

            List<Sequence> matches = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger index = new AtomicInteger(0);

            findIdentialToRemove(suis_ss2_1.getSequences(), genome, writer, matches, index);
            //popProcessGenome(suis_ss2_1.getSequences(), genome, writer, matches, index);
            log.info("Found " + matches.size() + " matches in " + genome.getOutputFilename());
            index.set(0);
            findIdentialToRemove(suis_ss2_1.getComplementSequences(), genome, writer, matches, index);
            //popProcessGenome(suis_ss2_1.getComplementSequences(), genome, writer, matches, index);
            log.info("Found " + matches.size() + " complement matches");

            suis_ss2_1.removeAll(matches);

            int countAfter = suis_ss2_1.getTotalSequences();
            numberOfFilesProcessed++;
            log.info("Processed " + numberOfFilesProcessed + "/" + files.size() + " Removed: " + (countBefore - countAfter));

            writer.close();
        }
        suis_ss2_1.writeSequences(outputFolder);
    }
    private static void findIdentialToRemove(Collection<Sequence> suis_ss2_1, Genome genome, BufferedWriter writer, List<Sequence> matches, AtomicInteger index) {
        suis_ss2_1.parallelStream().forEach(sequence -> {
            if(genome.exists(sequence))
            {
                try {
                    writer.append(sequence.toString() + " removed because an identical sequence exists\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                matches.add(sequence);
            }
            index.incrementAndGet();
            log.info(index + "/" + (suis_ss2_1.size()));
        });
    }

    private static void popProcessGenome(Collection<Sequence> suis_ss2_1, Genome genome, BufferedWriter writer, List<Sequence> matches, AtomicInteger index) {
        suis_ss2_1.parallelStream().forEach(sequence -> {
            SequenceEvaluator matchingEvaluator = genome.hasAnyMatchToAnyEvaluator(Arrays.asList(
                    new IdenticalEvaluator(sequence)));
            if(matchingEvaluator != null)
            {
                try {
                    writer.append(sequence.toString() + " removed by " + matchingEvaluator.toString() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                matches.add(sequence);
            }
            index.incrementAndGet();
            log.info(index + "/" + (suis_ss2_1.size()));
        });
    }

    private static void runJake() throws Exception {
        String inputFolder = "input/jake/";
        String outputFolder = "output/" + new Date().toString() + " Jake/";
        String outputInputFolder = outputFolder + "input/";
        // Create output folders
        new File(outputFolder).mkdirs();
        new File(outputInputFolder).mkdirs();

        File genomeFeatureFile = new File(inputFolder + "CP018908.1 feature table.txt");
        FileUtils.copyFile(genomeFeatureFile, new File(outputFolder+genomeFeatureFile.getName()));
        GenomeFeature genomeFeature =new GenomeFeature(genomeFeatureFile);

        File suisGenomeFile = new File(inputFolder + "Suis strain SS2-1 sequence.fasta");
        FileUtils.copyFile(suisGenomeFile, new File(outputFolder+suisGenomeFile.getName()));
        Genome suis_ss2_1 = new Genome(suisGenomeFile, Collections.singletonList(new CrisprPamEvaluator()), false);

        List<Genome> genomes = loadGenomes(inputFolder+"genomes/", Collections.singletonList(new CrisprPamEvaluator()));
        writeGenomes(genomes, outputInputFolder);

        processJake(new File(inputFolder+"jake_pam_only_mism_v2.csv"), suis_ss2_1, genomes, outputFolder, genomeFeature);
        processJake(new File(inputFolder+"jake_seed_only_mism_v2.csv"), suis_ss2_1, genomes, outputFolder, genomeFeature);
        processJake(new File(inputFolder+"jake_seed_andPam_mism_v2.csv"), suis_ss2_1,genomes, outputFolder, genomeFeature);
    }

    private static void processJake(File file, Genome suis_ss2_1, List<Genome> genomes,
                                    String outputFolder, GenomeFeature genomeFeature) throws IOException, CsvException {
        JakeCsv jakeCsv = SequenceReader.JakeCsvReader(file);

        for(int i=0; i<jakeCsv.getRows().size(); i++) {
            Sequence sequence = jakeCsv.getRowSequence(i);
            List<Sequence> matchedSequences = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger matchedGenomes = new AtomicInteger();
            genomes.parallelStream().forEach(genome -> {
                List<Sequence> matches = genome.getSequencesMatchingAnyEvaluator(
                        Collections.singletonList(new IdenticalEvaluator(sequence)));
                if(!matches.isEmpty()) {
                    matchedSequences.addAll(matches);
                    matchedGenomes.getAndIncrement();
                }
            });
            if(matchedGenomes.get() == genomes.size()) {
                log.info("matches in all genomes");

                List<Sequence> suisMaches = suis_ss2_1.getSequencesMatchingAnyEvaluator(
                        Collections.singletonList(new IdenticalEvaluator(sequence)));
                List<Feature> suisFeatures = genomeFeature.getMatchingFeatures(suisMaches);

                jakeCsv.addMatches(matchedSequences, i, suisMaches, suisFeatures);
            }
            log.info(i + "/" + jakeCsv.getRows().size());
        }
        jakeCsv.writeToFile(outputFolder + file.getName().replace(".csv", "_results.csv"));
    }

    private static List<Genome> loadGenomes(String path, List<SequenceEvaluator> criteria) throws Exception {
        List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());
        List<File> genomeFiles = getFilesInFolder(path, ".fasta");
        (DEBUG ? genomeFiles.stream() : genomeFiles.parallelStream())
                .forEach(file -> {
            try {
                genomes.add(new Genome(file, criteria, false));
            } catch (Exception e) {
                log.severe("Error creating genome from file " + file.getAbsolutePath() + " " + e.getMessage());
                System.exit(1);
            }
        });
        return genomes;
    }

    private static void writeGenomes(List<Genome> genomes, String outputFolder) {
        genomes.parallelStream().forEach(genome -> {
            try {
                genome.writeSequences(outputFolder);
            } catch (Exception e) {
                log.severe("Error when processing genomes and saving sequences: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    private static List<File> getFilesInFolder(String folder, String suffix) throws Exception {
        File directoryPath = new File(folder);
        List<File> results = new ArrayList();
        for(File file: directoryPath.listFiles()) {
            if(file.isFile()) {
                if(file.getName().endsWith(suffix)) {
                    results.add(file);
                }
            } else if(file.isDirectory()) {
                results.addAll(getFilesInFolder(file.getPath(), suffix));
            } else {
                throw new Exception("Something whent wrong when reading all files");
            }
        }
        return results;
    }

    private static void printMemoryStat() {
        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory() / MEGABYTE_FACTOR;
        long free = rt.freeMemory() / MEGABYTE_FACTOR;

        long used = total - free;
        if(used > maxMemUsage) {
            maxMemUsage = used;
            log.info("Total Memory: " + total + " MB, Used: " + used + ", Free: " + free + ", MaxUsed: " + maxMemUsage);
        }
    }

}
