package com.henrikroslund.pipeline.stage;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.Genome;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.GCContentN1N20Evaluator;
import com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;

import static com.henrikroslund.Utils.FASTA_FILE_ENDING;
import static com.henrikroslund.Utils.loadGenomesInFolder;
import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.Type.QUADRUPLE;

/**
 * This stage will analyze all genomes in the input folder and produce a singe genome output
 * with sequences which met all the criteria according the stage configuration.
 *
 * This can be particularly useful to select initial crispr candidates in a pipeline.
 */

@Log
public class CrisprSelection extends Stage {

    private final List<SequenceEvaluator> filters = new ArrayList<>();

    private final boolean skipDuplicates;
    private final boolean mergeChromosomes = true;

    public CrisprSelection(boolean useConsecutiveEvaluator, boolean useGcContentEvaluator, boolean skipDuplicates) {
        super(CrisprSelection.class);
        this.skipDuplicates = skipDuplicates;

        filters.add(new CrisprPamEvaluator(true));
        if(useConsecutiveEvaluator) {
            filters.add(new NoConsecutiveIdenticalN1N20Evaluator(QUADRUPLE));
        }
        if(useGcContentEvaluator) {
            Range<Integer> gcContentRange = Range.between(8, 13);
            filters.add(new GCContentN1N20Evaluator(gcContentRange));
        }
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<Genome> genomes = loadGenomesInFolder(inputFolder, filters, skipDuplicates, mergeChromosomes);
        Genome result;
        if(genomes.isEmpty()) {
            throw new Exception("Could not find any genomes in " + inputFolder);
        } else if(genomes.size() == 1) {
            result = genomes.get(0);
        } else {
            log.info("Found " + genomes.size() + " files in " + inputFolder + " so will build a genome with all sequences");
            result = new Genome(skipDuplicates, "merged"+FASTA_FILE_ENDING, "");
            for(Genome genome : genomes) {
                result.addAll(genome.getSequences());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        description.append(" skipDuplicates=").append(skipDuplicates);
        description.append(" mergeChromosomes=").append(mergeChromosomes);
        for(SequenceEvaluator evaluator : filters) {
            description.append(" ").append(evaluator.describe());
        }
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/reference_sequence";
    }
}
