package com.henrikroslund;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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
        List<File> genomeFiles = Utils.getFilesInFolder(folder, FASTA_FILE_ENDING);
        genomeFiles.addAll(Utils.getFilesInFolder(folder, Genome.GENOME_FILE_ENDING));
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

    public static void threadDump() {
        BufferedWriter threadDumpWriter = null;
        try {
            String outputFolder = Main.baseOutputFolder + "/debugging";
            new File(outputFolder).mkdirs();
            threadDumpWriter = new BufferedWriter(new FileWriter(outputFolder + "/"
                    + new SimpleDateFormat("yyyy-MM-dd hhmmss aa z").format(new Date()) + ".log", true));
            StringBuilder threadDump = new StringBuilder(System.lineSeparator());
            threadDump.append("Number of threads ").append(Thread.activeCount()).append(System.lineSeparator());
            threadDump.append("Total Number of threads ").append(ManagementFactory.getThreadMXBean().getThreadCount()).append(System.lineSeparator());
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            for(ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
                threadDump.append(threadInfo.toString());
            }
            threadDumpWriter.write(threadDump.toString());
        } catch (IOException e) {
            log.severe("Error when saving thread dump: " + e.getMessage());
        } finally {
            try {
                if(threadDumpWriter != null) {
                    threadDumpWriter.close();
                }
            } catch (IOException e) {
                log.severe("Error when closing writer: " + e.getMessage());
            }
        }
    }
}
