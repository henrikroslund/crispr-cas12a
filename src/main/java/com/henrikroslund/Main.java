package com.henrikroslund;

import com.henrikroslund.evaluators.IdenticalEvaluator;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;

    private static final String INPUT_FOLDER = "input/";

    public static final String OUTPUT_FOLDER = "output/" + new Date().toString() + "/";
    public static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";

    private static List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        long start = new Date().getTime();
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        new File(OUTPUT_FOLDER).mkdirs();

        loadGenomes();
        writeGenomes();

        List<Sequence> mainSequences = SequenceReader.SequenceReader(new File("Cas12a_gRNAs_10xgenomes_-_All.csv"));

        AtomicInteger processedSequences = new AtomicInteger(0);
        mainSequences.parallelStream().forEach(sequence -> {
            AtomicInteger matchedGenomes = new AtomicInteger(0);
            genomes.stream().forEach(genome -> {
                List<Sequence> matchingSequences = genome.getMatchingSequences(new IdenticalEvaluator(sequence));
                if(!matchingSequences.isEmpty()) {
                    matchedGenomes.incrementAndGet();
                }
            });
            if(matchedGenomes.get() == genomes.size()) {
                log.info("matches in all genomes");
            }
            log.info("processed: " + processedSequences.incrementAndGet() + " / " + mainSequences.size());
        });

        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
    }

    private static void loadGenomes() {
        File[] genomeFiles = getFilesInFolder(INPUT_FOLDER);
        Arrays.stream(genomeFiles).parallel().forEach(file -> {
            try {
                genomes.add(new Genome(file));
            } catch (Exception e) {
                log.severe("Error creating genome from file " + file.getAbsolutePath());
                System.exit(1);
            }
        });
    }

    private static void writeGenomes() {
        genomes.parallelStream().forEach(genome -> {
            try {
                genome.saveSequences();
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
        Runtime rt = Runtime.getRuntime();

        long total = rt.totalMemory() / MEGABYTE_FACTOR;
        long free = rt.freeMemory() / MEGABYTE_FACTOR;

        long used = total - free;
        log.info("Total Memory: " + total + " MB, Used: " + used + ", Free: " + free);
    }

}
