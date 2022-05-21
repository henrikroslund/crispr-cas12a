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
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This stage will analyze the input genome sequences with the genomes in the input folder
 * according to the given TypeEvaluator.
 * The input folder sequences can be reduced by providing a set of Evaluators for the sample set,
 * this can greatly increase performance of the analysis.
 * The typing results are stored in each sequence in the input genome during the process.
 * Since the process can take a very long time, intermediate results will be saved to file
 * along with a list of already processed genomes from the input folder. In this way that
 * could be used to resume the pipeline at a later time if needed.
 */

@Log
public class CandidateTyping extends Stage {

    private static final String PROCESSED_GENOMES_FILE = "genomesProcessed";
    private final boolean ENABLED_ALREADY_PROCESSED_FILE = false;

    private final SequenceEvaluator bindCriteria;
    private final List<SequenceEvaluator> sampleSetCriteria;
    private final TypeEvaluator typeEvaluator;
    private final boolean saveSurroundingSequencesForMatches;

    public CandidateTyping(List<SequenceEvaluator> sampleSetCriteria, SequenceEvaluator bindCriteria,
                           TypeEvaluator typeEvaluator, boolean saveSurroundingSequencesForMatches) {
        super(CandidateTyping.class);
        this.bindCriteria = bindCriteria;
        this.sampleSetCriteria = sampleSetCriteria;
        this.typeEvaluator = typeEvaluator;
        this.saveSurroundingSequencesForMatches = saveSurroundingSequencesForMatches;
    }

    public CandidateTyping(List<SequenceEvaluator> sampleSetCriteria, TypeEvaluator typeEvaluator) {
        this(sampleSetCriteria, new MatchEvaluator(null, Range.between(15, 24),
                Collections.singletonList(Range.between(Sequence.SEED_INDEX_START, Sequence.RAW_INDEX_END))), typeEvaluator,
                false);
    }

    public CandidateTyping() {
        this(Collections.emptyList(), new TypeEvaluator(null));
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        Collection<String> alreadyProcessed = getAlreadyProcessedGenomes(inputFolder);

        int fileNumber = 0;
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder, Utils.FASTA_FILE_ENDING);
        log.info("Will process genomes in following order:");
        for(File file : otherGenomes) {
            log.info(file.getName());
        }

        for(File file : otherGenomes) {
            fileNumber++;
            BufferedWriter processedGenomeWriter = new BufferedWriter(new FileWriter(outputFolder + "/" + PROCESSED_GENOMES_FILE, true));
            if(alreadyProcessed.contains(file.getName())) {
                log.info("Already processed file so skipping: " + file.getName());
                processedGenomeWriter.write(file.getName());
                processedGenomeWriter.newLine();
                processedGenomeWriter.close();
                continue;
            }
            Date startTime = new Date();
            Genome genome = new Genome(file, sampleSetCriteria, true, false);
            AtomicInteger counter = new AtomicInteger(0);

            Collection<Sequence> discards = Collections.synchronizedSet(new TreeSet<>());
            inputGenome.getSequences().parallelStream().forEach(mainGenomeSequence -> {

                Collection<Sequence> allMatchesInOtherGenomes =
                        genome.getSequencesMatchingAnyEvaluator(bindCriteria.getNewEvaluator(mainGenomeSequence));

                if(allMatchesInOtherGenomes.isEmpty()) {
                    log.info("There were no matches for sequence " + mainGenomeSequence.toString() + " in genome " + genome.getFilename());
                    mainGenomeSequence.increaseMetaDataCounters(Collections.singletonList(TypeEvaluator.Type.TYPE_4));
                }

                allMatchesInOtherGenomes.forEach(sequence -> {
                    TypeEvaluator evaluator = (TypeEvaluator) typeEvaluator.getNewEvaluator(mainGenomeSequence);
                    evaluator.evaluate(sequence);
                    mainGenomeSequence.increaseMetaDataCounters(evaluator.getMatchTypes());
                    if(evaluator.getMatchTypes().contains(TypeEvaluator.Type.TYPE_DISCARD)) {
                        discards.add(mainGenomeSequence);
                    }
                    log.info("allMatches: " + allMatchesInOtherGenomes.size() + " " + mainGenomeSequence + " " + evaluator + " discardCount: " + discards.size());
                    if(saveSurroundingSequencesForMatches) {
                        genome.saveSurroundingSequences(sequence, outputFolder, mainGenomeSequence.getGenome() + "_" + genome.getFilename() + "_" +  sequence.getRaw() + Utils.FASTA_FILE_ENDING);
                    }
                });

                counter.incrementAndGet();
                if (counter.get() % 10 == 0) {
                    log.info(" Counter: " + counter + "/" + inputGenome.getSequences().size());
                }
            });

            inputGenome.removeAll(discards);
            log.info("Candidate size: " + inputGenome.getTotalSequences());
            log.info("Finished processing file " + fileNumber + "/" + otherGenomes.size() + " in " + (new Date().getTime() - startTime.getTime()) / 1000 + " seconds");
            processedGenomeWriter.write(file.getName()+"\n");
            processedGenomeWriter.close();
            inputGenome.writeSequences(outputFolder, "candidates_files_processed_"+fileNumber);
            if (inputGenome.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + inputGenome.getTotalSequences());
        return inputGenome;
    }

    private static Collection<String> getAlreadyProcessedGenomes(String inputFolder) throws Exception {
        Collection<String> files = new TreeSet<>();
        File file = new File(inputFolder+PROCESSED_GENOMES_FILE);
        if(!file.exists()) {
            log.info("File with already processed genomes does not exist. No input files will be skipped");
            return files;
        }
        Path filePath = Path.of(file.getAbsolutePath());
        BufferedReader reader = Files.newBufferedReader(filePath);
        reader.lines().forEach(files::add);
        log.info("Previously processed file count: " + files.size());
        return files;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        description.append(" sampleSetCriteria=");
        sampleSetCriteria.forEach(evaluator -> description.append(evaluator.describe()));
        description.append(" bindCriteria=").append(bindCriteria.describe());
        description.append(" typeCriteria=").append(typeEvaluator.describe());
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/cross_reactive_pathogens";
    }
}
