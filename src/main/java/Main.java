import lombok.extern.java.Log;

import java.io.File;
import java.util.*;

@Log
public class Main {

    private static final long MEGABYTE_FACTOR = 1024L * 1024L;

    private static final String INPUT_FOLDER = "input/";

    public static final String OUTPUT_FOLDER = "output/" + new Date().toString() + "/";
    public static final String OUTPUT_FOLDER_INVALID = OUTPUT_FOLDER + "invalid/";
    public static final String OUTPUT_INVALID_SUFFIX = "_invalid";
    public static final String OUTPUT_COMPLEMENT_SUFFIX = "_complement";
    public static final String OUTPUT_RESULT_SUFFIX = "_result";

    private static final boolean WRITE_INVALID = false;

    private static List<Genome> genomes = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        new File(OUTPUT_FOLDER).mkdirs();
        if(WRITE_INVALID) {
            new File(OUTPUT_FOLDER_INVALID).mkdirs();
        }

        File[] genomeFiles = getFilesInFolder(INPUT_FOLDER);
        Arrays.stream(genomeFiles).parallel().forEach(file -> {
            try {
                genomes.add(new Genome(file, WRITE_INVALID));
            } catch (Exception e) {
                log.severe("Error creating genome from file " + file.getAbsolutePath());
                System.exit(1);
            }
        });

        genomes.parallelStream().forEach(genome -> {
            try {
                genome.processGenomes(genomes);
                genome.saveSequences();
            } catch (Exception e) {
                log.severe("Error when processing genomes and saving sequences: " + e.getMessage());
                System.exit(1);
            }
        });

        printMemoryStat();
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
