package com.henrikroslund.configuration.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.henrikroslund.Utils.isChromosomeFile;
import static com.henrikroslund.Utils.isPrimaryChromosomeFile;

@Log
public class CrisprCommon extends Stage {

    private static final String stageFolder = "/strains";

    public CrisprCommon(String inputFolder, String baseOutputFolder) {
        super(CrisprCommon.class.getSimpleName(), inputFolder, baseOutputFolder, stageFolder);
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + "/discarded.sequences", true));

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, ".fasta");
        boolean mergeAllChromosomes = true;
        for(File file : genomeFiles) {
            if(mergeAllChromosomes && isChromosomeFile(file.getAbsolutePath()) && !isPrimaryChromosomeFile(file.getAbsolutePath())) {
                log.info("Will skip file because it is not primary chromosome " + file.getName());
                continue;
            }
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true, mergeAllChromosomes);
            AtomicInteger counter = new AtomicInteger(0);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(!genome.exists(sequence)) {
                    notFound.add(sequence);
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info("NotFound: " + notFound.size() + " Counter: " + counter + "/" + inputGenome.getSequences().size());
                }
            });
            printProcessingTime(startTime);
            for(Sequence sequence : notFound) {
                discardWriter.append(sequence.toString() + " removed because it was NOT found in " + file.getName() + "\n");
            }
            inputGenome.removeAll(notFound);

            log.info("Candidate size " + inputGenome.getTotalSequences() + " after removing " + notFound.size() + " sequences not found in file " + file.getName() );
            if(inputGenome.getTotalSequences() == 0) {
                log.info("No candidates left so will stop");
                break;
            }
        }
        discardWriter.close();
        return inputGenome;
    }

    private void printProcessingTime(Date startTime) {
        long durationSeconds = (new Date().getTime() - startTime.getTime())/1000;
        if(durationSeconds > 30) {
            log.info("Finished processing file in " + durationSeconds + " seconds");
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
