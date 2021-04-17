package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class CrisprElimination extends Stage {

    public CrisprElimination() {
        super(CrisprElimination.class);
    }

    @Override
    protected String getStageFolder() {
        return "/cross_reactive_pathogens";
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder, ".fasta");
        int fileNumber = 0;
        for(File file : otherGenomes) {
            Collection<Sequence> found =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.emptyList(), true, false);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    found.add(sequence);
                }
            });

            printProcessingTime(startTime);
            writeDiscarded(found, " removed because it was found in " + file.getName());
            inputGenome.removeAll(found);

            log.info("Candidate size " + inputGenome.getTotalSequences() + " after removing " + found.size() + " sequences found in file " + fileNumber++ + "/" + otherGenomes.size() + " " + file.getName() );
            if(inputGenome.getTotalSequences() == 0) {
                log.info("No candidates left so will stop");
                break;
            }
        }
        return inputGenome;
    }

    @Override
    public String toString() {
        return getName();
    }
}
