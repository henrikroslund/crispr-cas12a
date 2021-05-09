package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;

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
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder, Utils.FASTA_FILE_ENDING);
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
                    // TODO change this back to being an OR operator and instead the
                    // MismatchEvaluator should handle the AND conditions so it can print the correct
                    // log matching for ====X==XXX
                    List<SequenceEvaluator> newEvaluators = SequenceEvaluator.getNewEvaluators(sequence, evaluators);
                    Sequence match = genome.getSequenceMatchingAllEvaluators(newEvaluators);
                    if(match != null) {
                        found.add(sequence);
                        log.info("Will remove " + sequence + " because close match was found by " + newEvaluators);
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
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        for(SequenceEvaluator evaluator : evaluators) {
            description.append(" ").append(evaluator.describe());
        }
        return description.toString();
    }
}
