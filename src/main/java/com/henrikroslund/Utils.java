package com.henrikroslund;

import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Log
public class Utils {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;
    static long maxMemUsage = 0;

    public static String getFirstRow(String filename) throws Exception {
        Path filePath = Path.of(filename);
        try(Stream<String> lines = Files.lines(filePath)) {
            return lines.findFirst().orElseThrow() + "\n";
        }
    }

    public static List<File> getFolders(String path) {
        File directoryPath = new File(path);
        List<File> results = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(directoryPath.listFiles())).sorted().forEach(file -> {
            if(file.isDirectory()) {
                results.add(file);
            }
        });
        return results;
    }

    public static String getStringWithoutWhitespaces(String string) {
        return string.replaceAll("\\s","");
    }

    public static List<File> getFilesInFolder(String path, String suffix) {
        File directoryPath = new File(path);
        List<File> results = new ArrayList<>();
        Arrays.stream(Objects.requireNonNull(directoryPath.listFiles())).sorted().forEach(file -> {
            if(file.isFile()) {
                if(file.getName().endsWith(suffix)) {
                    results.add(file);
                }
            } else if(file.isDirectory()) {
                results.addAll(getFilesInFolder(file.getPath(), suffix));
            } else {
                log.severe("Something went wrong when reading all files");
                System.exit(1);
            }
        });
        return results;
    }

    public static void throwIfFileExists(String path) throws Exception {
        File file = new File(path);
        if(file.exists()) {
            throw new Exception("File should not have existed but did: " + path);
        }
    }

    public static List<Genome> loadGenomesInFolder(String folder, List<SequenceEvaluator> criteria, boolean skipDuplicates, boolean includeAllChromosomes) {
        List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());
        List<File> genomeFiles = Utils.getFilesInFolder(folder, "");
        (Main.DEBUG ? genomeFiles.stream() : genomeFiles.parallelStream())
                .forEach(file -> {
                    try {
                        genomes.add(new Genome(file, criteria, skipDuplicates, includeAllChromosomes));
                    } catch (Exception e) {
                        log.severe("Error creating genome from file " + file.getAbsolutePath() + " " + e.getMessage());
                        System.exit(1);
                    }
                });
        return genomes;
    }

    public static final String FASTA_FILE_ENDING = ".fasta";
    protected final static String CHROMOSOME_STRING = "chromosome";
    private final static Pattern CHROMOSOME_PATTERN = Pattern.compile(".*"+CHROMOSOME_STRING+"\\s[0-9]"+FASTA_FILE_ENDING+"$");
    public static boolean isChromosomeFile(String filename) {
        Matcher matcher = CHROMOSOME_PATTERN.matcher(filename);
        return matcher.find();
    }

    public static boolean isPrimaryChromosomeFile(String filename) {
        return isChromosomeFile(filename) && filename.charAt(filename.length()-FASTA_FILE_ENDING.length()-1) == '1';
    }

    public static ArrayList<String> getChromosomeFiles(String filename) {
        int maxChromosomefiles = 5;
        ArrayList<String> files = new ArrayList<>(maxChromosomefiles);
        if(!isChromosomeFile(filename)) {
            throw new IllegalArgumentException("Tried to get chromosome files");
        }
        String base = filename.substring(0, filename.length()-FASTA_FILE_ENDING.length()-1);
        for( int i = 1; i <= maxChromosomefiles; i++ ) {
            files.add(base + i + FASTA_FILE_ENDING );
        }
        return files;
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
