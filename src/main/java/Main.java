import lombok.extern.java.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private static List<Genome> genomes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");

        log.info("Started Crispr-cas12a");

        // Create output folders
        new File(OUTPUT_FOLDER).mkdirs();
        if(WRITE_INVALID) {
            new File(OUTPUT_FOLDER_INVALID).mkdirs();
        }

        File[] genomeFiles = getFilesInFolder(INPUT_FOLDER);
        for(File genomeFile : genomeFiles) {
            genomes.add(new Genome(genomeFile, WRITE_INVALID));
        }

        for(Genome genome : genomes) {
            genome.processGenomes(genomes);
            genome.saveSequences();
        }

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
