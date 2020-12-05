package com.henrikroslund;

import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Log
public class Utils {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    static long maxMemUsage = 0;

    public static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of(filename);
        Stream<String> lines = Files.lines(filePath);
        return lines.findFirst().get() + "\n";
    }

    public static List<File> getFilesInFolder(String folder, String suffix) {
        File directoryPath = new File(folder);
        List<File> results = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(directoryPath.listFiles())).sorted().forEach(file -> {
            if(file.isFile()) {
                if(file.getName().endsWith(suffix)) {
                    results.add(file);
                }
            } else if(file.isDirectory()) {
                results.addAll(getFilesInFolder(file.getPath(), suffix));
            } else {
                log.severe("Something whent wrong when reading all files");
                System.exit(1);
            }
        });
        return results;
    }

    public static List<Genome> loadGenomes(String path, List<SequenceEvaluator> criteria) throws Exception {
        List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());
        List<File> genomeFiles = Utils.getFilesInFolder(path, ".fasta");
        (Main.DEBUG ? genomeFiles.stream() : genomeFiles.parallelStream())
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

    public static void writeGenomes(List<Genome> genomes, String outputFolder) {
        genomes.parallelStream().forEach(genome -> {
            try {
                genome.writeSequences(outputFolder);
            } catch (Exception e) {
                log.severe("Error when processing genomes and saving sequences: " + e.getMessage());
                System.exit(1);
            }
        });
    }

    public static void printMemoryStat() {
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
