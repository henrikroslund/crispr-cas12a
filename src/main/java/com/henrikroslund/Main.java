package com.henrikroslund;

import com.henrikroslund.evaluators.*;
import com.henrikroslund.evaluators.comparisons.MatchEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.formats.PopCsv;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.genomeFeature.GenomeFeature;
import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;

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
import java.util.logging.SimpleFormatter;

import static com.henrikroslund.Utils.*;

@Log
public class Main {

    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> memUsageHandle = scheduler.scheduleAtFixedRate(Utils::printMemoryStat, 1, 5, TimeUnit.SECONDS);

    static final boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        long start = new Date().getTime();

        String inputFolder = "input/pop/";
        String baseOutputFolder = "output/" + new Date().toString() + " pop/";
        String baseOutputInputFolder = baseOutputFolder + "input/";
        // Create output folders
        new File(baseOutputFolder).mkdirs();
        new File(baseOutputInputFolder).mkdirs();

        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        FileHandler fh = new FileHandler(baseOutputFolder + "application.log");
        fh.setFormatter(simpleFormatter);
        log.addHandler(fh);

        log.info("Started Crispr-cas12a");

        String inputSuis = "Suis strain SS2-1 sequence_candidates_processTypes_15";

        for(int minMatches = 15; minMatches<= 15; minMatches++) {
            String outputFolder = baseOutputFolder + " minMatches_"+minMatches+"/";
            String outputInputFolder = outputFolder + "input/";
            new File(outputFolder).mkdirs();
            new File(outputInputFolder).mkdirs();

            FileHandler tmpLogHandler = new FileHandler(outputFolder + "application" + minMatches + ".log");
            tmpLogHandler.setFormatter(simpleFormatter);
            log.addHandler(tmpLogHandler);

            GenomeFeature genomeFeature = createGenomeFeatures(inputFolder, outputInputFolder);

            Genome suis_ss2_1 = getPopSuis(inputFolder, outputInputFolder, inputSuis, true);
            log.info("Read suis genome with " + suis_ss2_1.getTotalSequences() + " sequences");
            /*
            alignWithSuis(suis_ss2_1, outputFolder, inputFolder);
            removeIdenticalMatchesWithAllGenomes(suis_ss2_1, outputFolder, inputFolder);

            removeTooSimilarSequences(suis_ss2_1, outputFolder, inputFolder);
            */
            processTypes(suis_ss2_1, outputFolder, inputFolder,
                    new MatchEvaluator(null, Range.between(minMatches, 24),
                            Collections.singletonList(Range.between(Sequence.SEED_INDEX_START, Sequence.RAW_INDEX_END))), "_"+minMatches);

            Genome suisWithDuplicates = getPopSuis(inputFolder, outputInputFolder, false);
            processFeatures(suis_ss2_1, suisWithDuplicates, genomeFeature, outputFolder);

            log.removeHandler(tmpLogHandler);
        }

        memUsageHandle.cancel(false);
        scheduler.shutdown();
        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
    }

    private static void processFeatures(Genome suis_candidates, Genome suisWithDuplicates, GenomeFeature genomeFeature, String outputFolder) throws Exception {
        log.info("processFeatures");
        PopCsv popCsv = new PopCsv();

        for(Sequence candidate : suis_candidates.getSequences()) {
            List<Sequence> suisMaches = suisWithDuplicates.getSequencesMatchingAnyEvaluator(
                    Collections.singletonList(new IdenticalEvaluator(candidate)));

            if(suisMaches.size()  > 1) {
                log.info("suisMatches:" + suisMaches.size() + " " + candidate.toString());
            }

            // We need to maintain the meta data so add it for all
            for(Sequence sequence : suisMaches) {
                sequence.setMetaData(candidate.getMetaData());
            }

            List<Feature> suisFeatures = genomeFeature.getMatchingFeatures(suisMaches, false);
            popCsv.addFeatureMatches(suisMaches, suisFeatures);
        }
        popCsv.writeToFile(outputFolder + suis_candidates.getOutputFilename() + "_with_features.csv");
    }

    private static GenomeFeature createGenomeFeatures(String inputFolder, String outputInputFolder) throws Exception {
        log.info("createGenomeFeatures");
        File genomeFeatureFile = new File(inputFolder + "CP018908.1 feature table.txt");
        FileUtils.copyFile(genomeFeatureFile, new File(outputInputFolder+genomeFeatureFile.getName()));
        GenomeFeature genomeFeature = new GenomeFeature(genomeFeatureFile);
        return genomeFeature;
    }

    private static Genome processTypes(Genome suis_ss2_1, String outputFolder, String inputFolder, SequenceEvaluator bindCriteria, String outputSuffix) throws Exception {
        log.info("processTypes");
        // Remove any duplicates found in other genomes
        int fileNumber = 0;
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");
        for(File file : otherGenomes) {
            fileNumber++;
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);

            suis_ss2_1.getSequences().parallelStream().forEach(suisSequence -> {

                SequenceEvaluator evaluator = null;
                if(bindCriteria instanceof MismatchEvaluator) {
                    evaluator = new MismatchEvaluator(suisSequence, ((MismatchEvaluator) bindCriteria).getMismatchRange());
                } else if(bindCriteria instanceof MatchEvaluator) {
                    evaluator = new MatchEvaluator(suisSequence, ((MatchEvaluator) bindCriteria).getMatchRange());
                } else {
                    log.severe("Not supported match evaluator");
                    System.exit(1);
                }

                Collection<Sequence> allMatchesInOtherGenomes = genome.getSequencesMatchingAnyEvaluator(evaluator);

                if(allMatchesInOtherGenomes.isEmpty()) {
                    log.info("There were not matches for sequence " + suisSequence.toString() + " in genome " + genome.getOutputFilename());
                    suisSequence.increaseMetaDataCounter(TypeEvaluator.Type.TYPE_4);
                }

                allMatchesInOtherGenomes.forEach(sequence -> {
                    TypeEvaluator typeEvaluator = new TypeEvaluator(suisSequence);
                    typeEvaluator.evaluate(sequence);
                    suisSequence.increaseMetaDataCounter(typeEvaluator.getMatchType());
                    log.info("allMatches: " + allMatchesInOtherGenomes.size() + " " + suisSequence.toString() + " " + typeEvaluator.toString());
                });

                counter.incrementAndGet();
                if (counter.get() % 10 == 0) {
                    log.info(" Counter: " + counter + "/" + suis_ss2_1.getSequences().size());
                }
            });

            log.info("Finished processing file " + fileNumber + " in " + (new Date().getTime() - startTime.getTime()) / 1000 + " seconds");
            if (suis_ss2_1.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        suis_ss2_1.writeSequences(outputFolder, "_candidates_processTypes" + outputSuffix);
        return suis_ss2_1;
    }

    private static Genome alignWithSuis(Genome suis_ss2_1, String outputFolder, String inputFolder) throws Exception {
        log.info("alignWithSuis");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + suis_ss2_1.getOutputFilename() + "_evaluations_alignWithSuis", true));

        // Only keep the ones which also exist in all the suis genomes
        List<File> suisFiles = Utils.getFilesInFolder(inputFolder+"suisGenomes/", ".fasta");
        for(File file : suisFiles) {
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);
            suis_ss2_1.getSequences().parallelStream().forEach(sequence -> {
                if(!genome.exists(sequence)) {
                    notFound.add(sequence);
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info("NotFound: " + notFound.size() + " Counter: " + counter + "/" + suis_ss2_1.getSequences().size());
                }
            });
            log.info("Finished processing file in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds");
            log.info("Will remove " + notFound.size() + " sequences which was not found in file " + file.getName());
            for(Sequence sequence : notFound) {
                discardWriter.append(sequence.toString() + " removed because it was NOT found in " + file.getName() + "\n");
            }
            suis_ss2_1.removeAll(notFound);
            log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        }
        discardWriter.close();
        suis_ss2_1.writeSequences(outputFolder, "_candidates_alignWithSuis");
        return suis_ss2_1;
    }

    private static Genome removeIdenticalMatchesWithAllGenomes(Genome suis_ss2_1, String outputFolder, String inputFolder) throws  Exception {
        log.info("removeIdenticalMatchesWithAllGenomes");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + suis_ss2_1.getOutputFilename() + "_evaluations_removeIdenticalMatchesWithAllGenomes", true));
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");
        for(File file : otherGenomes) {
            Collection<Sequence> found =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);
            suis_ss2_1.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    found.add(sequence);
                }
                counter.incrementAndGet();
                if (counter.get() % 100 == 0) {
                    log.info("Found: " + found.size() + " Counter: " + counter + "/" + suis_ss2_1.getSequences().size());
                }
            });
            log.info("Finished processing file in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds");
            log.info("Will remove " + found.size() + " sequences which was found in file " + file.getName());
            for(Sequence sequence : found) {
                discardWriter.append(sequence.toString()).append(" removed because it was found in ").append(file.getName()).append("\n");
            }
            suis_ss2_1.removeAll(found);
            log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        }
        discardWriter.close();
        return suis_ss2_1;
    }

    private static Genome removeTooSimilarSequences(Genome suis_ss2_1, String outputFolder, String inputFolder) throws Exception {
        log.info("removeTooSimilarSequences");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + suis_ss2_1.getOutputFilename() + "_removeTooSimilarSequences", true));

        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");

        List<Range<Integer>> pamNoVAndSeedIndexes = Arrays.asList(Range.between(Sequence.PAM_INDEX_START,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));

        // Go through all the genomes and remove the ones which has sequence which are too similar to the candidates
        int fileNumber = 0;
        for (File file : otherGenomes) {
            fileNumber++;
            Collection<Sequence> found = Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            AtomicInteger counter = new AtomicInteger(0);
            Genome genome = new Genome(file, Collections.emptyList(), true);
            suis_ss2_1.getSequences().parallelStream().forEach(suisSequence -> {
                SequenceEvaluator pamAndSeedEval = new MismatchEvaluator(suisSequence, Range.between(0,3), pamNoVAndSeedIndexes);
                SequenceEvaluator n7to20Eval = new MismatchEvaluator(suisSequence, Range.between(0,2), Range.between(10,23));
                for (Sequence genomeSequence : genome.getSequences()) {
                    if (pamAndSeedEval.evaluate(genomeSequence) && n7to20Eval.evaluate(genomeSequence)) {
                        found.add(suisSequence);
                        log.info("Marked for removal: " + suisSequence.toString() + " " + pamAndSeedEval.toString() + " " + n7to20Eval.toString());
                        break;
                    }
                }
                counter.incrementAndGet();
                if (counter.get() % 100 == 0) {
                    log.info("Found: " + found.size() + " Counter: " + counter + "/" + suis_ss2_1.getSequences().size());
                }
            });
            log.info("Finished processing file " + fileNumber + " in " + (new Date().getTime() - startTime.getTime()) / 1000 + " seconds");
            log.info("Will remove " + found.size() + " sequences which was found in file " + file.getName());
            for (Sequence sequence : found) {
                discardWriter.append(sequence.toString()).append(" removed because it was found in ").append(file.getName()).append("\n");
            }
            suis_ss2_1.removeAll(found);
            log.info("Candidate size: " + suis_ss2_1.getTotalSequences());

            if (suis_ss2_1.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + suis_ss2_1.getTotalSequences());
        suis_ss2_1.writeSequences(outputFolder, "_result_removeTooSimilarSequences");

        discardWriter.close();
        return suis_ss2_1;
    }

    private static Genome getPopSuis(String inputFolder, String outputInputFolder, boolean skipDuplicates) throws Exception {
        return getPopSuis(inputFolder, outputInputFolder, "Suis strain SS2-1 sequence.fasta", skipDuplicates);
    }

    private static Genome getPopSuis(String inputFolder, String outputInputFolder, String filename, boolean skipDuplicates) throws Exception {
        // Read the original suis genome
        File suisGenomeFile = new File(inputFolder + filename);
        FileUtils.copyFile(suisGenomeFile, new File(outputInputFolder+suisGenomeFile.getName()));
        Range gcContentRange = Range.between(8, 12);
        log.info("Using gcContentRange: " + gcContentRange);
        Genome suis_ss2_1 = filename.endsWith(".fasta") ?
                new Genome(suisGenomeFile, Arrays.asList(new CrisprPamEvaluator(), new NoTripletN1N20Evaluator(), new GCContentN1N20Evaluator(gcContentRange)), skipDuplicates) :
                Genome.loadGenome(suisGenomeFile);
        suis_ss2_1.writeSequences(outputInputFolder, "_sequences");
        return suis_ss2_1;
    }
}
