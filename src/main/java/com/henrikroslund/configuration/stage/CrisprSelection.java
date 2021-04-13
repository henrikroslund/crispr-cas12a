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
    private final boolean includeAllChromosomes = true;
    private static final String stageFolder = "/reference_sequence";

    public CrisprSelection(boolean useConsecutiveEvaluator, boolean useGcContentEvaluator, boolean skipDuplicates, String inputFolder, String baseOutputFolder) {
        super(CrisprSelection.class.getSimpleName(), inputFolder, baseOutputFolder, stageFolder);
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
        List<Genome> genomes = loadGenomes(inputFolder, filters, skipDuplicates, includeAllChromosomes);
        Genome result;
        if(genomes.isEmpty()) {
            throw new Exception("Could not find any genomes in " + inputFolder);
        } else if(genomes.size() == 1) {
            result = genomes.get(0);
        } else {
            log.info("Found multiple files in " + stageFolder + " so will build a genome with all sequences");
            result = new Genome(skipDuplicates, "result.fasta", "");
            for(Genome genome : genomes) {
                result.addAll(genome.getSequences());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
