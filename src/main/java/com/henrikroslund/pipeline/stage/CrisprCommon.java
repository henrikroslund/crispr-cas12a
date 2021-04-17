package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Main;
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

    public CrisprCommon() {
        super(CrisprCommon.class);
    }

    public CrisprCommon(int n7n20AllowedMismatches) {
        this();
        this.n7n20AllowedMismatches = n7n20AllowedMismatches;
    }

    @Override
    protected String getStageFolder() {
        return "/strains";
    }

    private boolean exists(Genome genome, Sequence sequence) {
        if(genome.exists(sequence)) {
            return true;
        } else if(n7n20AllowedMismatches > 0) {
            List<SequenceEvaluator> evaluators = new ArrayList<>();
            // We do not check pam because we know that all sequences are already Crispr sequences
            evaluators.add(new IdenticalEvaluator(sequence, false, true, false));
            evaluators.add(new MismatchEvaluator(sequence, Range.between(0, n7n20AllowedMismatches), Range.between(Sequence.SEED_INDEX_END, Sequence.RAW_INDEX_END)));
            Sequence matchingSequence = genome.getSequenceMatchingAllEvaluators(evaluators);
            if(matchingSequence != null) {
                log.info("Found match for sequence " + sequence + " with " + matchingSequence);
                return true;
            }
        }
        return false;
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, ".fasta");
        for(File file : genomeFiles) {
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.singletonList(new CrisprPamEvaluator()), true, false);
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
        description.append(getName()).append(" n7n20AllowedMismatches=").append(n7n20AllowedMismatches);
        return description.toString();
    }

}
