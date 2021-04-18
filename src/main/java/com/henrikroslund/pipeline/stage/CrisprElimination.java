package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class CrisprElimination extends Stage {

    List<SequenceEvaluator> evaluators = Collections.emptyList();

    public CrisprElimination() {
        super(CrisprElimination.class);
    }

    public CrisprElimination(List<SequenceEvaluator> evaluators) {
        this();
        this.evaluators = evaluators;
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

            Genome genome = new Genome(file, Collections.singletonList(new CrisprPamEvaluator(false)), true, false);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    found.add(sequence);
                }
                if(!evaluators.isEmpty()) {
                    SequenceEvaluator evaluator = genome.hasAnyMatchToAnyEvaluator(SequenceEvaluator.getNewEvaluators(sequence, evaluators));
                    if(evaluator != null) {
                        found.add(sequence);
                        log.info("Will remove " + sequence + " because close match was found by " + evaluator.toString());
                    }
                }
            });

            printProcessingTime(startTime);
            writeDiscarded(found, " removed because evaluator match in " + file.getName());
            inputGenome.removeAll(found);

            log.info("Candidate size " + inputGenome.getTotalSequences() + " after removing " + found.size() + " sequences found in file " + ++fileNumber + "/" + otherGenomes.size() + " " + file.getName() );
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
