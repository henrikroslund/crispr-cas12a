package com.henrikroslund;

import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.formats.JakeCsv;
import com.opencsv.exceptions.CsvException;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
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
        processJake("jake_pam_only_mism_v2.csv");
        processJake("seed_only_mism_v2.csv");
        processJake("jake_seed_andPam_mism_v2.csv");

        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
    }

    private static void processJake(String file) throws IOException, CsvException {
        JakeCsv jakeCsv = SequenceReader.JakeCsvReader(new File(file));

        for(int i=0; i<jakeCsv.getRows().size(); i++) {
            Sequence sequence = jakeCsv.getRowSequence(i);
            List<Sequence> matchedSequences = new ArrayList<>();
            AtomicInteger matchedGenomes = new AtomicInteger();
            genomes.stream().forEach(genome -> {
                List<Sequence> matches = genome.getMatchingSequences(new IdenticalEvaluator(sequence));
                if(!matches.isEmpty()) {
                    matchedSequences.addAll(matches);
                    matchedGenomes.getAndIncrement();
                }
            });
            if(matchedGenomes.get() == genomes.size()) {
                log.info("matches in all genomes");
                jakeCsv.addMatches(matchedSequences, i);
            }
            log.info(i + "/" + jakeCsv.getRows().size());
        }
        jakeCsv.writeToFile(OUTPUT_FOLDER + file.replace(".csv", "_results.csv"));
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
