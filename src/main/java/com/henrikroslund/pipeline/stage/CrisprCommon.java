package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.io.File;
import java.util.*;

@Log
public class CrisprCommon extends Stage {

    private int n7n20AllowedMismatches = 0;
    private final List<SequenceEvaluator> evaluators = new ArrayList<>();

    // We include both chromosomes as one genome file because we don't want to require it to be in both chromosomes
    private final static boolean includeAllChromosomes = true;

    public CrisprCommon() {
        super(CrisprCommon.class);
    }

    public CrisprCommon(int n7n20AllowedMismatches) {
        this();
        this.n7n20AllowedMismatches = n7n20AllowedMismatches;
        // We do not check pam because we know that all sequences are already Crispr sequences
        evaluators.add(new IdenticalEvaluator(null, false, true, false));
        evaluators.add(new MismatchEvaluator(null, Range.between(0, n7n20AllowedMismatches), Range.between(Sequence.N7_INDEX, Sequence.N20_INDEX)));
    }

    @Override
    protected String getStageFolder() {
        return "/strains";
    }

    private boolean exists(Genome genome, Sequence sequence) {
        if(genome.exists(sequence)) {
            return true;
        } else if(n7n20AllowedMismatches > 0) {
            List<SequenceEvaluator> sequenceEvaluators = new ArrayList<>();
            evaluators.forEach(evaluator -> sequenceEvaluators.add(evaluator.getNewEvaluator(sequence)));
            Sequence matchingSequence = genome.getSequenceMatchingAllEvaluators(sequenceEvaluators);
            if(matchingSequence != null) {
                log.info("Found match for sequence " + sequence + " with " + matchingSequence);
                return true;
            }
        }
        return false;
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, Utils.FASTA_FILE_ENDING);
        for(File file : genomeFiles) {
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();

            if(Utils.isChromosomeFile(file.getName()) && !Utils.isPrimaryChromosomeFile(file.getName())) {
                log.info("Will skip file because it is not the primary chromosome file: " + file.getName());
                continue;
            }

            Genome genome = new Genome(file, Collections.singletonList(new CrisprPamEvaluator(true)), true, includeAllChromosomes);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(!exists(genome, sequence)) {
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
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        description.append(" n7n20AllowedMismatches=").append(n7n20AllowedMismatches);
        evaluators.forEach(evaluator -> description.append(" ").append(evaluator));
        return description.toString();
    }

}
