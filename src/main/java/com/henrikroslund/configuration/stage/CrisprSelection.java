package com.henrikroslund.configuration.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.GCContentN1N20Evaluator;
import com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;

import static com.henrikroslund.Utils.*;
import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.Type.*;

@Log
public class CrisprSelection extends Stage {

    List<SequenceEvaluator> filters = new ArrayList<>();

    private final boolean skipDuplicates;
    private final boolean mergeChromosomes = true;

    public CrisprSelection(boolean useConsecutiveEvaluator, boolean useGcContentEvaluator, boolean skipDuplicates) {
        super(CrisprSelection.class);
        this.skipDuplicates = skipDuplicates;

        filters.add(new CrisprPamEvaluator());
        if(useConsecutiveEvaluator) {
            filters.add(new NoConsecutiveIdenticalN1N20Evaluator(QUADRUPLE));
        }
        if(useGcContentEvaluator) {
            Range<Integer> gcContentRange = Range.between(8, 13);
            log.info("Using gcContentRange: " + gcContentRange);
            filters.add(new GCContentN1N20Evaluator(gcContentRange));
        }
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<Genome> genomes = loadGenomes(inputFolder, filters, skipDuplicates, mergeChromosomes);
        Genome result;
        if(genomes.isEmpty()) {
            throw new Exception("Could not find any genomes in " + inputFolder);
        } else if(genomes.size() == 1) {
            result = genomes.get(0);
        } else {
            log.info("Found " + genomes.size() + " files in " + inputFolder + " so will build a genome with all sequences");
            result = new Genome(skipDuplicates, "result.fasta", "");
            for(Genome genome : genomes) {
                result.addAll(genome.getSequences());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("skipDuplicates=").append(skipDuplicates).append("\n");
        description.append("mergeChromosomes=").append(mergeChromosomes);
        for(SequenceEvaluator evaluator : filters) {
            description.append("\nfilter: ").append(evaluator.describe());
        }
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/reference_sequence";
    }
}
