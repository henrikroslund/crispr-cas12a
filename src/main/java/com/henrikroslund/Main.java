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
import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.QUADRUPLE;
import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.TRIPLE;

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

        String originalMainGenomeFilename = "Suis strain SS2-1 sequence.fasta";
        String mainGenomeFilename = "Suis strain SS2-1 sequence_candidates_processTypes_15";

        for(int minMatches = 15; minMatches<= 15; minMatches++) {
            String outputFolder = baseOutputFolder + " minMatches_"+minMatches+"/";
            String outputInputFolder = outputFolder + "input/";
            new File(outputFolder).mkdirs();
            new File(outputInputFolder).mkdirs();

            FileHandler tmpLogHandler = new FileHandler(outputFolder + "application" + minMatches + ".log");
            tmpLogHandler.setFormatter(simpleFormatter);
            log.addHandler(tmpLogHandler);

            GenomeFeature genomeFeature = createGenomeFeatures(inputFolder, outputInputFolder);

            Genome mainGenome = getPopSuis(inputFolder, outputInputFolder, mainGenomeFilename, true);
            log.info("Read main genome with " + mainGenome.getTotalSequences() + " sequences");

            alignWithAllInFolder(mainGenome, outputFolder, inputFolder);
            removeIdenticalMatchesWithAllGenomes(mainGenome, outputFolder, inputFolder);

            // Excluded for now
            //removeTooSimilarSequences(mainGenome, outputFolder, inputFolder);

            processTypes(mainGenome, outputFolder, inputFolder,
                    new MatchEvaluator(null, Range.between(minMatches, 24),
                            Collections.singletonList(Range.between(Sequence.SEED_INDEX_START, Sequence.RAW_INDEX_END))), "_"+minMatches);

            Genome mainGenomeWithDuplicates = getPopSuis(inputFolder, outputInputFolder, originalMainGenomeFilename, false);
            processFeatures(mainGenome, mainGenomeWithDuplicates, genomeFeature, outputFolder);

            log.removeHandler(tmpLogHandler);
        }

        memUsageHandle.cancel(false);
        scheduler.shutdown();
        printMemoryStat();
        log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
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

            // We need to maintain the meta data so add it for all
            for(Sequence sequence : matches) {
                sequence.setMetaData(candidate.getMetaData());
            }

            List<Feature> features = genomeFeature.getMatchingFeatures(matches, false);
            popCsv.addFeatureMatches(matches, features);
        }
        popCsv.writeToFile(outputFolder + candidates.getOutputFilename() + "_with_features.csv");
    }

    private static GenomeFeature createGenomeFeatures(String inputFolder, String outputInputFolder) throws Exception {
        log.info("createGenomeFeatures");
        File genomeFeatureFile = new File(inputFolder + "CP018908.1 feature table.txt");
        FileUtils.copyFile(genomeFeatureFile, new File(outputInputFolder+genomeFeatureFile.getName()));
        GenomeFeature genomeFeature = new GenomeFeature(genomeFeatureFile);
        return genomeFeature;
    }

    private static Genome processTypes(Genome mainGenome, String outputFolder, String inputFolder, SequenceEvaluator bindCriteria, String outputSuffix) throws Exception {
        log.info("processTypes");
        // Remove any duplicates found in other genomes
        int fileNumber = 0;
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");
        for(File file : otherGenomes) {
            fileNumber++;
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);

            mainGenome.getSequences().parallelStream().forEach(mainGenomeSequence -> {

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
                    log.info("allMatches: " + allMatchesInOtherGenomes.size() + " " + mainGenomeSequence.toString() + " " + typeEvaluator.toString());
                });

                counter.incrementAndGet();
                if (counter.get() % 10 == 0) {
                    log.info(" Counter: " + counter + "/" + mainGenome.getSequences().size());
                }
            });

            log.info("Finished processing file " + fileNumber + " in " + (new Date().getTime() - startTime.getTime()) / 1000 + " seconds");
            if (mainGenome.getSequences().isEmpty()) {
                break;
            }
        }
        log.info("Candidate size: " + mainGenome.getTotalSequences());
        mainGenome.writeSequences(outputFolder, "_candidates_processTypes" + outputSuffix);
        return mainGenome;
    }

    private static Genome alignWithAllInFolder(Genome mainGenome, String outputFolder, String inputFolder) throws Exception {
        log.info("alignWithAllInFolder");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + mainGenome.getOutputFilename() + "_evaluations_alignWithAllInFolder", true));

        // Only keep the ones which also exist in all the genomes in the selected inputFolder
        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder+"suisGenomes/", ".fasta");
        for(File file : genomeFiles) {
            Collection<Sequence> notFound =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);
            mainGenome.getSequences().parallelStream().forEach(sequence -> {
                if(!genome.exists(sequence)) {
                    notFound.add(sequence);
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info("NotFound: " + notFound.size() + " Counter: " + counter + "/" + mainGenome.getSequences().size());
                }
            });
            log.info("Finished processing file in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds");
            log.info("Will remove " + notFound.size() + " sequences which was not found in file " + file.getName());
            for(Sequence sequence : notFound) {
                discardWriter.append(sequence.toString() + " removed because it was NOT found in " + file.getName() + "\n");
            }
            mainGenome.removeAll(notFound);
            log.info("Candidate size: " + mainGenome.getTotalSequences());
        }
        discardWriter.close();
        mainGenome.writeSequences(outputFolder, "_candidates_alignWithAllInFolder");
        return mainGenome;
    }

    private static Genome removeIdenticalMatchesWithAllGenomes(Genome mainGenome, String outputFolder, String inputFolder) throws  Exception {
        log.info("removeIdenticalMatchesWithAllGenomes");
        BufferedWriter discardWriter = new BufferedWriter(new FileWriter(outputFolder + mainGenome.getOutputFilename() + "_evaluations_removeIdenticalMatchesWithAllGenomes", true));
        List<File> otherGenomes = Utils.getFilesInFolder(inputFolder+"genomes/", ".fasta");
        for(File file : otherGenomes) {
            Collection<Sequence> found =  Collections.synchronizedSet(new HashSet<>());
            Date startTime = new Date();
            Genome genome = new Genome(file, Collections.emptyList(), true);
            AtomicInteger counter = new AtomicInteger(0);
            mainGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    found.add(sequence);
                }
                counter.incrementAndGet();
                if (counter.get() % 100 == 0) {
                    log.info("Found: " + found.size() + " Counter: " + counter + "/" + mainGenome.getSequences().size());
                }
            });
            log.info("Finished processing file in " + (new Date().getTime() - startTime.getTime())/1000 + " seconds");
            log.info("Will remove " + found.size() + " sequences which was found in file " + file.getName());
            for(Sequence sequence : found) {
                discardWriter.append(sequence.toString()).append(" removed because it was found in ").append(file.getName()).append("\n");
            }
            mainGenome.removeAll(found);
            log.info("Candidate size: " + mainGenome.getTotalSequences());
        }
        discardWriter.close();
        return mainGenome;
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
            Genome genome = new Genome(file, Collections.emptyList(), true);
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

    private static Genome getPopSuis(String inputFolder, String outputInputFolder, String filename, boolean skipDuplicates) throws Exception {
        Range gcContentRange = Range.between(8, 12);
        log.info("Using gcContentRange: " + gcContentRange);
        return readGenome(inputFolder, outputInputFolder, filename, skipDuplicates,
                Arrays.asList(new CrisprPamEvaluator(), new NoConsecutiveIdenticalN1N20Evaluator(TRIPLE), new GCContentN1N20Evaluator(gcContentRange)));
    }

    private static Genome getBP(String inputFolder, String outputInputFolder, boolean skipDuplicates) throws Exception {
        Range gcContentRange = Range.between(8, 12);
        log.info("Using gcContentRange: " + gcContentRange);
        return readGenome(inputFolder, outputInputFolder, "XXXXXXX.fasta", skipDuplicates,
                Arrays.asList(new CrisprPamEvaluator(), new NoConsecutiveIdenticalN1N20Evaluator(QUADRUPLE), new GCContentN1N20Evaluator(gcContentRange)));
    }

    private static Genome readGenome(String inputFolder, String outputInputFolder, String filename, boolean skipDuplicates, List<SequenceEvaluator> criteria) throws Exception {
        File genomeFile = new File(inputFolder + filename);
        FileUtils.copyFile(genomeFile, new File(outputInputFolder+genomeFile.getName()));
        Genome genome = filename.endsWith(".fasta") ?
                new Genome(genomeFile, criteria, skipDuplicates) :
                Genome.loadGenome(genomeFile);
        genome.writeSequences(outputInputFolder, "_sequences");
        return genome;
    }
}
