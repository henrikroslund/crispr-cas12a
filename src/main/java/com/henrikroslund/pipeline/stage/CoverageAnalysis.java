package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.henrikroslund.Utils.*;

@Log
public class CoverageAnalysis extends Stage {

    public CoverageAnalysis() {
        super(CoverageAnalysis.class);
    }

    private final Map<Sequence, HashSet<String>> coverageMap = new ConcurrentHashMap<>();

    private void initiateCoverageMap(Genome genome) {
        for(Sequence sequence : genome.getSequences()) {
            coverageMap.put(sequence, new HashSet<>());
        }
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        initiateCoverageMap(inputGenome);

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, FASTA_FILE_ENDING);
        int remainingFiles = genomeFiles.size();
        for(File file : genomeFiles) {
            if(isChromosomeFile(file.getAbsolutePath()) && !isPrimaryChromosomeFile(file.getAbsolutePath())) {
                log.info("Will skip file because it is not primary chromosome " + file.getName());
                continue;
            }
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.emptyList(), true, true);

            AtomicInteger counter = new AtomicInteger(0);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                // TODO need to take the right rules for match as input
                if(genome.exists(sequence)) {
                    coverageMap.get(sequence).add(file.getName());
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info(" Counter: " + counter + "/" + inputGenome.getSequences().size());
                }
            });
            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
            printResults();
        }

        return inputGenome;
    }

    private void printResults() {
        coverageMap.forEach((sequence, genomeMatches) -> log.info(sequence.toString() + " was found in " + genomeMatches.size() + " genomes"));
    }

    @Override
    public String toString() {
        return getName() + " " + getStageFolder();
    }

    @Override
    protected String getStageFolder() {
        return "/strains_coverage";
    }
}
