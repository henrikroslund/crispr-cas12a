package com.henrikroslund;

import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.pipeline.Pipeline;
import com.henrikroslund.pipeline.stage.*;
import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.Range;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.henrikroslund.Utils.printMemoryStat;

public class Main {

    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> memUsageHandle = scheduler.scheduleAtFixedRate(Utils::printMemoryStat, 1, 15, TimeUnit.SECONDS);

    public static final boolean DEBUG = false;
    public static FileHandler mainLoggerFileHandler;

    static final String baseOutputFolder = "../crispr-cas12a-output/" + new SimpleDateFormat("yyyy-MM-dd hhmmss z").format(new Date());
    static final String baseInputFolder = "../crispr-cas12a-input";

    private final static Logger log = Logger.getLogger("");

    public static void main(String[] args) {
        long start = new Date().getTime();
        try {
            if(!new File(baseOutputFolder).mkdirs()) {
                throw new Exception("Could not create output directory: " + baseOutputFolder);
            }
            setupLogging();
            log.info("Started Crispr-cas12a");

            //crisprBp04_17_21_optimized_pipline();
            //crisprBp04_17_21();
            //suisrRNA();
            //suisCommonCoverage();
            //rerunPartOfSuis();
            //performanceTesting();
            suis_pipeline_3();

        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.severe(sw.toString());
        } finally {
            memUsageHandle.cancel(false);
            scheduler.shutdown();
            printMemoryStat();
            log.info("Execution time: " + (new Date().getTime() - start)/1000 + " seconds");
        }
    }

    public static void crisprBp04_17_21_optimized_pipline() throws Exception {
        String inputFolder = baseInputFolder+"/CRIPSR Bp 04_17_21";

        List<SequenceEvaluator> eliminationEvaluators = new ArrayList<>();
        SequenceEvaluator seedEliminator = new MismatchEvaluator(null, Range.between(0,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));
        SequenceEvaluator n7N20Eliminator = new MismatchEvaluator(null, Range.between(0,4), Range.between(Sequence.N7_INDEX, Sequence.N20_INDEX));
        eliminationEvaluators.addAll(Arrays.asList(seedEliminator, n7N20Eliminator));

        Pipeline pipeline = new Pipeline("CRIPSR Bp 04_17_21_optimized_pipline", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        pipeline.addStage(new CrisprCommon(1));
        pipeline.addStage(new CrisprElimination(eliminationEvaluators));
        pipeline.addStage(new CandidateFeature());
        pipeline.run();
    }

    public static void crisprBp04_17_21() throws Exception {
        String inputFolder = baseInputFolder+"/CRIPSR Bp 04_17_21";
        Pipeline pipeline = new Pipeline("CRIPSR Bp 04_17_21", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        pipeline.addStage(new CrisprCommon(0));
        pipeline.addStage(new CrisprElimination());
        pipeline.addStage(new CandidateTyping());
        pipeline.addStage(new CandidateFeature());
        pipeline.run();
    }

    public static void suis_pipeline_3() throws Exception {
        String inputFolder = baseInputFolder+"/CRISPR Suis 04_20_21";
        Pipeline pipeline = new Pipeline("suis_pipeline_3", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        pipeline.addStage(new CrisprCommon(0));

        SequenceEvaluator n1N20Eliminator = new MismatchEvaluator(null, Range.is(0), Range.between(Sequence.TARGET_INDEX_START, Sequence.N20_INDEX));
        pipeline.addStage(new CrisprElimination(Arrays.asList(n1N20Eliminator)));

        SequenceEvaluator crisprEvaluator = new CrisprPamEvaluator(false);
        TypeEvaluator typeEvaluator = new TypeEvaluator(null,2,2,4,3);
        pipeline.addStage(new CandidateTyping(Arrays.asList(crisprEvaluator),typeEvaluator));

        pipeline.addStage(new CandidateFeature());
        pipeline.run();
    }

    public static void performanceTesting() throws Exception {
        String inputFolder = baseInputFolder+"/performance-testing";
        Pipeline pipeline = new Pipeline("Performance testing", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, true));
        for(int i=0; i<1; i++) {
            //pipeline.addStage(new CrisprSelection(false, false, true));
            //pipeline.addStage(new CrisprCommon());
            /*
            List<SequenceEvaluator> evaluators = new ArrayList<>();
            SequenceEvaluator seedEliminator = new MismatchEvaluator(null, Range.between(0,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));
            SequenceEvaluator n7N20Eliminator = new MismatchEvaluator(null, Range.between(0,4), Range.between(Sequence.N7_INDEX, Sequence.N20_INDEX));
            evaluators.add(seedEliminator);
            evaluators.add(n7N20Eliminator);
            pipeline.addStage(new CrisprElimination(evaluators));
             */
            pipeline.addStage(new CandidateTyping(Collections.singletonList(new CrisprPamEvaluator(false)), new TypeEvaluator(null)));
        }
        pipeline.run();
    }

    public static void suisrRNA() throws Exception {
        String inputFolder = baseInputFolder+"/CRISPR for Suis rRNA gene";
        Pipeline pipeline = new Pipeline("CRISPR for Suis rRNA gene", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, true));
        pipeline.addStage(new CrisprCommon());
        pipeline.addStage(new CrisprElimination());
        pipeline.addStage(new CandidateTyping());
        pipeline.run();
    }

    public static void suisCommonCoverage() throws Exception {
        String inputFolder = baseInputFolder+"/CRISPR for Suis rRNA gene";
        Pipeline pipeline = new Pipeline("CRISPR for Suis rRNA gene", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, true));
        pipeline.addStage(new CoverageAnalysis());
        pipeline.run();
    }

    public static void rerunPartOfSuis() throws Exception {
        String inputFolder = baseInputFolder+"/CRISPR Bm filter";
        Pipeline pipeline = new Pipeline("CRISPR Bm filter", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, true));
        pipeline.addStage(new CrisprElimination());
        pipeline.addStage(new CandidateTyping());
        pipeline.addStage(new CandidateFeature());
        pipeline.run();
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
                        log.info("Marked for removal: " + mainGenomeSequence.toString() + " " + pamAndSeedEval + " " + n7to20Eval);
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

    private static void setupLogging() throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        mainLoggerFileHandler = new FileHandler(baseOutputFolder + "/application.log");
        mainLoggerFileHandler.setFormatter(simpleFormatter);
        log.addHandler(mainLoggerFileHandler);
    }
}
