package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MatchEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class CandidateTyping extends Stage {

    private static final String PROCESSED_GENOMES_FILE = "genomesProcessed";
    private final boolean ENABLED_ALREADY_PROCESSED_FILE = false;

    public CandidateTyping() {
        super(CandidateTyping.class);
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        SequenceEvaluator bindCriteria = new MatchEvaluator(null, Range.between(15, 24),
                Collections.singletonList(Range.between(Sequence.SEED_INDEX_START, Sequence.RAW_INDEX_END)));

        HashSet<String> alreadyProcessed = ENABLED_ALREADY_PROCESSED_FILE ?
                getAlreadyProcessedGenomes(inputFolder) :
                new HashSet<>();

        int fileNumber = 0;
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder, ".fasta");
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
            FileUtils.copyFile(file, new File(outputInputFolder+"/"+file.getName()));
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true, false);
            AtomicInteger counter = new AtomicInteger(0);

            Collection<Sequence> discards = new HashSet<>();
            inputGenome.getSequences().parallelStream().forEach(mainGenomeSequence -> {

                SequenceEvaluator evaluator = null;
                if(bindCriteria instanceof MismatchEvaluator) {
                    evaluator = new MismatchEvaluator(mainGenomeSequence, ((MismatchEvaluator) bindCriteria).getMismatchRange(), ((MismatchEvaluator) bindCriteria).getIndexesToCompare());
                } else if(bindCriteria instanceof MatchEvaluator) {
                    evaluator = new MatchEvaluator(mainGenomeSequence, ((MatchEvaluator) bindCriteria).getMatchRange(), ((MatchEvaluator) bindCriteria).getIndexesToCompare());
                } else {
                    log.severe("Not supported match evaluator");
                    System.exit(1);
                }

                Collection<Sequence> allMatchesInOtherGenomes = genome.getSequencesMatchingAnyEvaluator(evaluator);

                if(allMatchesInOtherGenomes.isEmpty()) {
                    log.info("There were not matches for sequence " + mainGenomeSequence.toString() + " in genome " + genome.getOutputFilename());
                    mainGenomeSequence.increaseMetaDataCounter(TypeEvaluator.Type.TYPE_4);
                }

                allMatchesInOtherGenomes.forEach(sequence -> {
                    TypeEvaluator typeEvaluator = new TypeEvaluator(mainGenomeSequence);
                    typeEvaluator.evaluate(sequence);
                    mainGenomeSequence.increaseMetaDataCounter(typeEvaluator.getMatchType());
                    if(typeEvaluator.getMatchType() == TypeEvaluator.Type.TYPE_DISCARD) {
                        discards.add(mainGenomeSequence);
                    }
                    log.info("allMatches: " + allMatchesInOtherGenomes.size() + " " + mainGenomeSequence.toString() + " " + typeEvaluator.toString() + " discardCount: " + discards.size());
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
            inputGenome.writeSequences(outputFolder, "candidates_files_processed_"+fileNumber, ".sequences");
            if (inputGenome.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + inputGenome.getTotalSequences());
        return inputGenome;
    }

    private static HashSet<String> getAlreadyProcessedGenomes(String inputFolder) throws Exception {
        HashSet<String> files = new HashSet<>();
        File file = new File(inputFolder+PROCESSED_GENOMES_FILE);
        if(!file.exists()) {
            return files;
        }
        Path filePath = Path.of(file.getAbsolutePath());
        BufferedReader reader = Files.newBufferedReader(filePath);
        reader.lines().forEach(line -> {
            files.add(line);
        });
        log.info("Previously processed file count: " + files.size());
        return files;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    protected String getStageFolder() {
        return "/cross_reactive_pathogens";
    }
}
