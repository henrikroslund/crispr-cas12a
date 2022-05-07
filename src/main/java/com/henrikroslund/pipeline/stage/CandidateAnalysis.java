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
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.EvaluatorConfig;
import com.henrikroslund.evaluators.SequenceEvaluator;
import lombok.extern.java.Log;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Log
public class CandidateAnalysis extends Stage {

    private final SequenceEvaluator evaluator;
    private final boolean skipDuplicates = false;
    private final SequenceEvaluator genomeEvaluator = new CrisprPamEvaluator(false);

    public CandidateAnalysis(SequenceEvaluator evaluator) {
        super(CandidateAnalysis.class);
        this.evaluator = evaluator;
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        boolean logEvaluationMatchBefore = EvaluatorConfig.logEvaluationMatch;
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder, Utils.FASTA_FILE_ENDING);
        int fileNumber = 0;
        for(File file : otherGenomes) {
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.singletonList(genomeEvaluator), skipDuplicates, false);
            EvaluatorConfig.logEvaluationMatch = true;
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                SequenceEvaluator newEvaluator = evaluator.getNewEvaluator(sequence);
                genome.getSequencesMatchingAnyEvaluator(newEvaluator);
            });
            EvaluatorConfig.logEvaluationMatch = false;

            printProcessingTime(startTime);
            log.info("Finished processing file " + ++fileNumber + "/" + otherGenomes.size());
        }
        EvaluatorConfig.logEvaluationMatch = logEvaluationMatchBefore;
        return null;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        description.append(" GenomeReaderConfig[").append("skipDuplicates=" + skipDuplicates).append(" ").append(genomeEvaluator.describe()).append("]");
        for(SequenceEvaluator evaluator : List.of(evaluator)) {
            description.append(" ").append(evaluator.describe());
        }
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/cross_reactive_pathogens";
    }
}
