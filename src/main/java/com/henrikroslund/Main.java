package com.henrikroslund;

import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.formats.JakeCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import com.henrikroslund.sequence.SequenceReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
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

    public static void main(String[] args) throws Exception {
        long start = new Date().getTime();
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        runJake();

        memUsageHandle.cancel(false);
        scheduler.shutdown();
        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
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
        Genome suis_ss2_1 = new Genome(suisGenomeFile, true);

        List<Genome> genomes = loadGenomes(inputFolder+"genomes/", true);
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
                List<Sequence> matches = genome.getMatchingSequences(new IdenticalEvaluator(sequence));
                if(!matches.isEmpty()) {
                    matchedSequences.addAll(matches);
                    matchedGenomes.getAndIncrement();
                }
            });
            if(matchedGenomes.get() == genomes.size()) {
                log.info("matches in all genomes");
                List<Sequence> suisMaches = suis_ss2_1.getMatchingSequences(new IdenticalEvaluator(sequence));
                List<Feature> suisFeatures = genomeFeature.getMatchingFeatures(suisMaches);
                jakeCsv.addMatches(matchedSequences, i, suisMaches, suisFeatures);
            }
            log.info(i + "/" + jakeCsv.getRows().size());
        }
        jakeCsv.writeToFile(outputFolder + file.getName().replace(".csv", "_results.csv"));
    }

    private static List<Genome> loadGenomes(String path, boolean onlyCrisper) {
        List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());
        File[] genomeFiles = getFilesInFolder(path);
        Arrays.stream(genomeFiles).parallel().forEach(file -> {
            try {
                genomes.add(new Genome(file, onlyCrisper));
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

    private static File[] getFilesInFolder(String folder) {
        File directoryPath = new File(folder);
        return directoryPath.listFiles();
    }

    private static void printMemoryStat() {
        //System.gc();
        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory() / MEGABYTE_FACTOR;
        long free = rt.freeMemory() / MEGABYTE_FACTOR;

        long used = total - free;
        if(used > maxMemUsage) {
            maxMemUsage = used;
        }
        log.info("Total Memory: " + total + " MB, Used: " + used + ", Free: " + free + ", MaxUsed: " + maxMemUsage);
    }

}
