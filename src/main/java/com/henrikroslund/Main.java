package com.henrikroslund;

import com.henrikroslund.evaluators.*;
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
        runPop();

        memUsageHandle.cancel(false);
        scheduler.shutdown();
        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
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

        List<File> files = getFilesInFolder(inputFolder+"genomes/", ".fasta");
        log.info("Found " + files.size() + " genome files");
        int count = 0;
        // Loop over all genomes used for filtering and remove sequences which are disqualified from the original suis
        for(File file: files) {
            Genome genome = new Genome(file, Collections.EMPTY_LIST, true);
            int countBefore = suis_ss2_1.getSequences().size() + suis_ss2_1.getComplementSequences().size();
            log.info("Suis count remaining: " + countBefore);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "suis_sequences_removed_by_"+genome.getOutputFilename(), true));
            writer.append("Sequences in this file are suis ss2-1 which were removed by matches in genome " + genome.getOutputFilename() + "\n");

            List<Sequence> matches = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger index = new AtomicInteger(0);
            suis_ss2_1.getSequences().parallelStream().forEach(sequence -> {
                SequenceEvaluator matchingEvaluator = genome.hasAnyMatchToAnyEvaluator(
                        Arrays.asList(new IdenticalEvaluator(sequence), new PamAndSeedIdenticalMatcher(sequence)));
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
                log.info(index + "/" + (suis_ss2_1.getSequences().size()+suis_ss2_1.getComplementSequences().size()));
            });

            log.info("Found " + matches.size() + " matches in " + genome.getOutputFilename());

            suis_ss2_1.getComplementSequences().parallelStream().forEach(sequence -> {
                SequenceEvaluator matchingEvaluator = genome.hasAnyMatchToAnyEvaluator(
                        Arrays.asList(new IdenticalEvaluator(sequence), new PamAndSeedIdenticalMatcher(sequence)));
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
                log.info(index + "/" + (suis_ss2_1.getSequences().size()+suis_ss2_1.getComplementSequences().size()));
            });

            log.info("Found " + matches.size() + " complement matches");
            suis_ss2_1.removeAll(matches);

            int countAfter = suis_ss2_1.getSequences().size() + suis_ss2_1.getComplementSequences().size();
            count++;
            log.info("Processed " + count + "/" + files.size() + " Removed: " + (countBefore - countAfter));

            writer.close();
        }
        suis_ss2_1.writeSequences(outputFolder);
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
