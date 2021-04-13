package com.henrikroslund;

import com.henrikroslund.configuration.stage.CandidateTyping;
import com.henrikroslund.configuration.stage.CrisprCommon;
import com.henrikroslund.configuration.stage.CrisprElimination;
import com.henrikroslund.configuration.stage.CrisprSelection;
import com.henrikroslund.configuration.Pipeline;
import com.henrikroslund.evaluators.*;
import com.henrikroslund.evaluators.comparisons.MatchEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.formats.PopCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.henrikroslund.Utils.*;

public class Main {

    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> memUsageHandle = scheduler.scheduleAtFixedRate(Utils::printMemoryStat, 1, 15, TimeUnit.SECONDS);

    public static final boolean DEBUG = false;

    static final String baseOutputFolder = "output/" + new Date();

    public static void suisrRNA() throws Exception {
        String inputFolder = "input/CRISPR for Suis rRNA gene";
        Pipeline bp = new Pipeline("CRISPR for Suis rRNA gene", inputFolder, baseOutputFolder);
        bp.addStage(new CrisprSelection(false, false, true));
        bp.addStage(new CrisprCommon());
        bp.addStage(new CrisprElimination());
        bp.addStage(new CandidateTyping());
        bp.run();
    }

    private final static Logger log = Logger.getLogger("");

    public static void main(String[] args) throws Exception {
        long start = new Date().getTime();
        try {
            new File(baseOutputFolder).mkdirs();

            System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            FileHandler fh = new FileHandler(baseOutputFolder + "/application.log");
            fh.setFormatter(simpleFormatter);
            log.addHandler(fh);

            log.info("Started Crispr-cas12a");
            suisrRNA();
        } finally {
            memUsageHandle.cancel(false);
            scheduler.shutdown();
            printMemoryStat();
            log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
        }


/*
        List<File> candidateFiles = getFilesInFolder("input/bp/Bp rRNA gene", "fasta");
        for(File mainGenomeFile : candidateFiles) {
            String originalMainGenomeFilename = "Burkholderia pseudomallei strain K96243 chromosome 2.fasta";
            String featureFilename = "Feature2-Burkholderia pseudomallei strain Mahidol-1106a chromosome 2.txt";
            String mainGenomeFilename = "Bp rRNA gene/" + mainGenomeFile.getName();

            for(int minMatches = 15; minMatches<= 15; minMatches++) {
                String outputFolder = baseOutputFolder + " minMatches_"+minMatches+"/"+mainGenomeFile.getName()+"/";
                String outputInputFolder = outputFolder + "input/";
                new File(outputFolder).mkdirs();
                new File(outputInputFolder).mkdirs();

                FileHandler tmpLogHandler = new FileHandler(outputFolder + "application" + minMatches + ".log");
                tmpLogHandler.setFormatter(simpleFormatter);
                log.addHandler(tmpLogHandler);

                Genome mainGenome = getBP(inputFolder, outputInputFolder, mainGenomeFilename, true, true);
                log.info("Read main genome with " + mainGenome.getTotalSequences() + " sequences");

                removeAnyWithDiscardMetaData(mainGenome);

                alignWithAllInFolder(mainGenome, outputFolder, inputFolder);
                removeIdenticalMatchesWithAllGenomes(mainGenome, outputFolder, inputFolder);

                // Excluded for now for bp flow
                //removeTooSimilarSequences(mainGenome, outputFolder, inputFolder);

                processTypes(mainGenome, outputFolder, inputFolder, outputInputFolder,
                        new MatchEvaluator(null, Range.between(minMatches, 24),
                                Collections.singletonList(Range.between(Sequence.SEED_INDEX_START, Sequence.RAW_INDEX_END))), "_"+minMatches);

                GenomeFeature genomeFeature = createGenomeFeatures(inputFolder, outputInputFolder, featureFilename);
                Genome mainGenomeWithDuplicates = getBP(inputFolder, outputInputFolder, originalMainGenomeFilename, false, false);
                processFeatures(mainGenome, mainGenomeWithDuplicates, genomeFeature, outputFolder);

                log.removeHandler(tmpLogHandler);
            }
        }
*/
    }

    private static void removeAnyWithDiscardMetaData(Genome candidates) {
        Collection<Sequence> discards = new HashSet<>();
        candidates.getSequences().forEach(sequence -> {
            if(sequence.getMetaData() != null &&
               sequence.getMetaData().get(TypeEvaluator.Type.TYPE_DISCARD) != null &&
               sequence.getMetaData().get(TypeEvaluator.Type.TYPE_DISCARD)> 0) {
                discards.add(sequence);
            }
        });
        candidates.removeAll(discards);
        log.info("Candidate size: " + candidates.getSequences().size());
    }

    private static void processFeatures(Genome candidates, Genome mainGenomeWithDuplicates, GenomeFeature genomeFeature, String outputFolder) throws Exception {
        log.info("processFeatures");
        PopCsv popCsv = new PopCsv();

        for(Sequence candidate : candidates.getSequences()) {
            List<Sequence> matches = mainGenomeWithDuplicates.getSequencesMatchingAnyEvaluator(
                    Collections.singletonList(new IdenticalEvaluator(candidate)));

            if(matches.size()  > 1) {
                log.info("Matches:" + matches.size() + " " + candidate.toString());
            }
            if(matches.isEmpty()) {
                log.warning("No matches for sequence: " + candidate.toString());
            }

            // We need to maintain the meta data so add it for all
            for(Sequence sequence : matches) {
                sequence.setMetaData(candidate.getMetaData());
            }

            List<Feature> features = genomeFeature.getMatchingFeatures(matches, false);
            popCsv.addFeatureMatches(matches, features);
        }
        popCsv.writeToFile(outputFolder + mainGenomeWithDuplicates.getOutputFilename() + "_candidates_with_features.csv");
    }

    private static GenomeFeature createGenomeFeatures(String inputFolder, String outputInputFolder, String filename) throws Exception {
        log.info("createGenomeFeatures");
        File genomeFeatureFile = new File(inputFolder + filename);
        FileUtils.copyFile(genomeFeatureFile, new File(outputInputFolder+genomeFeatureFile.getName()));
        GenomeFeature genomeFeature = new GenomeFeature(genomeFeatureFile);
        return genomeFeature;
    }

    private static Genome removeTooSimilarSequences(Genome mainGenome, String outputFolder, String inputFolder) throws Exception {
        log.info("removeTooSimilarSequences");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + mainGenome.getOutputFilename() + "_removeTooSimilarSequences", true));

        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");

        List<Range<Integer>> pamNoVAndSeedIndexes = Arrays.asList(Range.between(Sequence.PAM_INDEX_START,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));

        // Go through all the genomes and remove the ones which has sequence which are too similar to the candidates
        int fileNumber = 0;
        for (File file : otherGenomes) {
            fileNumber++;
            Collection<Sequence> found = Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            AtomicInteger counter = new AtomicInteger(0);
            Genome genome = new Genome(file, Collections.emptyList(), true, false);
            mainGenome.getSequences().parallelStream().forEach(mainGenomeSequence -> {
                SequenceEvaluator pamAndSeedEval = new MismatchEvaluator(mainGenomeSequence, Range.between(0,3), pamNoVAndSeedIndexes);
                SequenceEvaluator n7to20Eval = new MismatchEvaluator(mainGenomeSequence, Range.between(0,2), Range.between(10,23));
                for (Sequence genomeSequence : genome.getSequences()) {
                    if (pamAndSeedEval.evaluate(genomeSequence) && n7to20Eval.evaluate(genomeSequence)) {
                        found.add(mainGenomeSequence);
                        log.info("Marked for removal: " + mainGenomeSequence.toString() + " " + pamAndSeedEval.toString() + " " + n7to20Eval.toString());
                        break;
                    }
                }
                counter.incrementAndGet();
                if (counter.get() % 100 == 0) {
                    log.info("Found: " + found.size() + " Counter: " + counter + "/" + mainGenome.getSequences().size());
                }
            });
            log.info("Finished processing file " + fileNumber + " in " + (new Date().getTime() - startTime.getTime()) / 1000 + " seconds");
            log.info("Will remove " + found.size() + " sequences which was found in file " + file.getName());
            for (Sequence sequence : found) {
                discardWriter.append(sequence.toString()).append(" removed because it was found in ").append(file.getName()).append("\n");
            }
            mainGenome.removeAll(found);
            log.info("Candidate size: " + mainGenome.getTotalSequences());

            if (mainGenome.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + mainGenome.getTotalSequences());
        mainGenome.writeSequences(outputFolder, "_result_removeTooSimilarSequences");

        discardWriter.close();
        return mainGenome;
    }
}
