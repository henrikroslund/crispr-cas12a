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
import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;

/**
 * This stage is used for eliminating crispr candidates by analysing genomes in the input folder.
 * The input genome contains the candidates and the result is a genome containing only the sequences
 * which remain after discarded sequences have been removed based on the list of evaluators configured.
 *
 * This is particularly useful to remove cross reactive pathogens from the crispr candidates.
 */

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
            Collection<Sequence> found =  Collections.synchronizedSet(new TreeSet<>());
            Date startTime = new Date();

            Genome genome = new Genome(file, Collections.singletonList(new CrisprPamEvaluator(false)), true, false);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    found.add(sequence);
                    IdenticalEvaluator evaluator = new IdenticalEvaluator(sequence);
                    Sequence match = genome.getSequenceMatchingAllEvaluators(List.of(evaluator));
                    log.info("Found exact match for " + sequence + " in " + match);
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
