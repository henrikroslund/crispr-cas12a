package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.henrikroslund.Utils.isChromosomeFile;
import static com.henrikroslund.Utils.isPrimaryChromosomeFile;

@Log
public class CrisprCommon extends Stage {

    public CrisprCommon() {
        super(CrisprCommon.class);
    }

    @Override
    protected String getStageFolder() {
        return "/strains";
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, ".fasta");
        for(File file : genomeFiles) {
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.emptyList(), true, false);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(!genome.exists(sequence)) {
                    notFound.add(sequence);
                }
            });

            printProcessingTime(startTime);
            writeDiscarded(notFound, " removed because it was NOT found in " + file.getName());
            inputGenome.removeAll(notFound);

            log.info("Candidate size " + inputGenome.getTotalSequences() + " after removing " + notFound.size() + " sequences not found in file " + file.getName() );
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
