package com.henrikroslund;

import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MatchEvaluator;
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
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.henrikroslund.Utils.printMemoryStat;

public class Main {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final static ScheduledFuture<?> memUsageHandle = scheduler.scheduleAtFixedRate(Utils::printMemoryStat, 1, 15, TimeUnit.SECONDS);

    public static final boolean DEBUG = false;
    public static FileHandler mainLoggerFileHandler;

    static final String baseOutputFolder = "../crispr-cas12a-output/" + new SimpleDateFormat("yyyy-MM-dd hhmmss aa z").format(new Date());
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
            //suis_pipeline_3();
            //testFastaSplit();
            //bpHumanGenome();
            //suisCoverage();
            serotyping();

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

    public static void serotyping() throws Exception {
        String inputFolder = baseInputFolder+"/Serotyping suis";
        Pipeline pipeline = new Pipeline("Serotyping suis", inputFolder, baseOutputFolder);
        pipeline.addStage(new Serotyping());
        pipeline.run();
    }

    public static void bpHumanGenome() throws Exception {
        String inputFolder = baseInputFolder+"/Checking bp human genome";
        Pipeline pipeline = new Pipeline("Checking bp human genome", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        SequenceEvaluator crisprEvaluator = new CrisprPamEvaluator(true);
        pipeline.addStage(new CandidateTyping(
                Collections.singletonList(crisprEvaluator),
                new MatchEvaluator(null, Range.between(17, 20), Collections.singletonList(Range.between(Sequence.N1_INDEX, Sequence.N20_INDEX))),
                new TypeEvaluator(null, 0, 0, 0, 0),
                true));
        pipeline.run();
    }

    public static void testFastaSplit() throws Exception {
        String inputFolder = baseInputFolder+"/test fasta splitting";
        Pipeline pipeline = new Pipeline("test fasta splitting", inputFolder, baseOutputFolder);
        CandidateTyping stage = new CandidateTyping();
        pipeline.addStage(stage);
        pipeline.preProcessStagesInput();
    }

    public static void crBP6() throws Exception {
        String inputFolder = baseInputFolder+"/Checking crBP6";
        Pipeline pipeline = new Pipeline("Checking crBP6", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        pipeline.addStage(new CandidateTyping(
                Collections.emptyList(),
                new MismatchEvaluator(null, Range.between(0, 3)),
                new TypeEvaluator(null, 0, 0, 0, 0),
                false));
        pipeline.run();
    }

    public static void crisprBp04_17_21_optimized_pipline() throws Exception {
        String inputFolder = baseInputFolder+"/CRIPSR Bp 04_17_21";

        SequenceEvaluator seedEliminator = new MismatchEvaluator(null, Range.between(0,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));
        SequenceEvaluator n7N20Eliminator = new MismatchEvaluator(null, Range.between(0,4), Range.between(Sequence.N7_INDEX, Sequence.N20_INDEX));
        List<SequenceEvaluator> eliminationEvaluators = new ArrayList<>(Arrays.asList(seedEliminator, n7N20Eliminator));

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
        String inputFolder = baseInputFolder+"/Bp 05_01_21";
        Pipeline pipeline = new Pipeline("suis_pipeline_3", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true));
        pipeline.addStage(new CrisprCommon(0));

        SequenceEvaluator n1N20Eliminator = new MismatchEvaluator(null, Range.is(0), Range.between(Sequence.TARGET_INDEX_START, Sequence.N20_INDEX));
        pipeline.addStage(new CrisprElimination(Collections.singletonList(n1N20Eliminator)));

        SequenceEvaluator crisprEvaluator = new CrisprPamEvaluator(false);
        TypeEvaluator typeEvaluator = new TypeEvaluator(null,2,2,4,3);
        pipeline.addStage(new CandidateTyping(Collections.singletonList(crisprEvaluator),typeEvaluator));

        pipeline.addStage(new CandidateFeature());
        pipeline.run();
    }

    public static void performanceTesting() throws Exception {
        for(int i=0; i<1; i++) {
            String inputFolder = baseInputFolder+"/performance-testing";
            Pipeline pipeline = new Pipeline("Performance testing", inputFolder, baseOutputFolder);
            pipeline.addStage(new CrisprSelection(false, false, true));

            pipeline.addStage(new CrisprSelection(false, false, true));
            pipeline.addStage(new CrisprCommon());

            List<SequenceEvaluator> evaluators = new ArrayList<>();
            SequenceEvaluator seedEliminator = new MismatchEvaluator(null, Range.between(0,2), Range.between(Sequence.SEED_INDEX_START, Sequence.SEED_INDEX_END));
            SequenceEvaluator n7N20Eliminator = new MismatchEvaluator(null, Range.between(0,4), Range.between(Sequence.N7_INDEX, Sequence.N20_INDEX));
            evaluators.add(seedEliminator);
            evaluators.add(n7N20Eliminator);
            pipeline.addStage(new CrisprElimination(evaluators));
            pipeline.addStage(new CandidateTyping(Collections.singletonList(new CrisprPamEvaluator(false)), new TypeEvaluator(null)));

            pipeline.run();
        }
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

    public static void suisCoverage() throws Exception {
        String inputFolder = baseInputFolder+"/Coverage suis";
        Pipeline pipeline = new Pipeline("Coverage suis", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, true));
        pipeline.addStage(new CoverageAnalysis(), false);
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

    private static void setupLogging() throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        mainLoggerFileHandler = new FileHandler(baseOutputFolder + "/application.log");
        mainLoggerFileHandler.setFormatter(simpleFormatter);
        log.addHandler(mainLoggerFileHandler);
    }
}
