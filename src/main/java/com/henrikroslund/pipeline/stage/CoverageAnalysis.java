package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.henrikroslund.Utils.isChromosomeFile;
import static com.henrikroslund.Utils.isPrimaryChromosomeFile;

@Log
public class CoverageAnalysis extends Stage {

    private final boolean mergeAllChromosomes = true;

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

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, ".fasta");
        for(File file : genomeFiles) {
            if(mergeAllChromosomes && isChromosomeFile(file.getAbsolutePath()) && !isPrimaryChromosomeFile(file.getAbsolutePath())) {
                log.info("Will skip file because it is not primary chromosome " + file.getName());
                continue;
            }
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.emptyList(), true, mergeAllChromosomes);

            AtomicInteger counter = new AtomicInteger(0);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    coverageMap.get(sequence).add(file.getName());
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info(" Counter: " + counter + "/" + inputGenome.getSequences().size());
                }
            });
            printProcessingTime(startTime);
        }
        // TOOD calculate coverage and stuff and save to file
        return inputGenome;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/strains_coverage";
    }
}
