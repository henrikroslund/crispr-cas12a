package com.henrikroslund;

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

import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.pipeline.Pipeline;
import com.henrikroslund.pipeline.stage.*;
import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.Range;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    static String inputFolder;

    static final String PIPELINE_ENV_KEY = "PIPELINE";
    static final String PIPELINE_INPUT_FOLDER = "PIPELINE_INPUT";

    enum PipelineConfiguration {
        PIPELINE_FEATURE("features"),
        PIPELINE_BP("bp"),
        PIPELINE_SUIS("suis"),
        PIPELINE_PERFORMANCE_TESTING("performance"),
        PIPELINE_TEST_PIPELINE_PREPROCESSING("test-preprocessing"),
        PIPELINE_DEFAULT("default");


        public final String value;

        PipelineConfiguration(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static PipelineConfiguration fromString(String stringValue) {
            for(PipelineConfiguration configuration : PipelineConfiguration.values()) {
                if(configuration.value.equalsIgnoreCase(stringValue)) {
                    return configuration;
                }
            }
            return null;
        }
    }

    private final static Logger log = Logger.getLogger("");


    public static void main(String[] args) {
        long start = new Date().getTime();
        try {
            if(!new File(baseOutputFolder).mkdirs()) {
                throw new Exception("Could not create output directory: " + baseOutputFolder);
            }
            setupLogging();
            log.info("Started Crispr-cas12a");

            var pipeline = System.getenv(PIPELINE_ENV_KEY);
            if(pipeline == null) {
                throw new IllegalArgumentException("Please set the environment variable " + PIPELINE_ENV_KEY + " to one of " + Arrays.toString(PipelineConfiguration.values()));
            }

            PipelineConfiguration configuration = PipelineConfiguration.fromString(pipeline);
            if(configuration == null) {
                throw new IllegalArgumentException("Invalid Pipeline \"" + pipeline + "\" Please set the environment variable " + PIPELINE_ENV_KEY + " to one of " + Arrays.toString(PipelineConfiguration.values()));
            }

            var pipelineInputFolder = System.getenv(PIPELINE_INPUT_FOLDER);
            if(pipelineInputFolder == null) {
                throw new IllegalArgumentException("Invalid input folder \"" + inputFolder + "\" Please set the environment variable " + PIPELINE_INPUT_FOLDER);
            }
            inputFolder = baseInputFolder + "/" + pipelineInputFolder;

            switch (configuration) {
                case PIPELINE_DEFAULT -> defaultPipeline();
                case PIPELINE_BP -> bp_pipeline();
                case PIPELINE_SUIS -> suis_pipeline();
                case PIPELINE_FEATURE -> featurePipeline();
                case PIPELINE_PERFORMANCE_TESTING -> performanceTesting();
                case PIPELINE_TEST_PIPELINE_PREPROCESSING -> testPipelinePreprocessing();
                default -> throw new IllegalArgumentException("Invalid PIPELINE selected: " + configuration);
            }

            //crisprBp04_17_21_optimized_pipline();
            //crisprBp04_17_21();
            //suisrRNA();
            //suisCommonCoverage();1
            //rerunPartOfSuis();
            //testFastaSplit();
            //bpHumanGenome();
            //suisCoverage();
            //serotyping();

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

    public static void defaultPipeline() throws Exception {
        Pipeline pipeline = new Pipeline("Pipeline name", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true), false);
        pipeline.addStage(new CandidateFeature(), false);
        pipeline.run();
    }

    public static void featurePipeline() throws Exception {
        Pipeline pipeline = new Pipeline("Feature pipeline", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(false, false, false), false);
        pipeline.addStage(new CandidateFeature(), false);
        pipeline.run();
    }

    public static void bp_pipeline() throws Exception {
        Pipeline pipeline = new Pipeline("bp_pipeline", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true), false);
        pipeline.addStage(new CrisprCommon(0, true, true, 0), false);

        SequenceEvaluator n1N20Eliminator = new MismatchEvaluator(null, Range.is(0), Range.between(Sequence.TARGET_INDEX_START, Sequence.N20_INDEX));
        pipeline.addStage(new CrisprElimination(Collections.singletonList(n1N20Eliminator)), false);

        SequenceEvaluator crisprEvaluator = new CrisprPamEvaluator(false);
        TypeEvaluator typeEvaluator = new TypeEvaluator(null,2,2,4,3);
        pipeline.addStage(new CandidateTyping(Collections.singletonList(crisprEvaluator),typeEvaluator), false);

        pipeline.addStage(new CandidateFeature(), false);
        pipeline.run();
    }

    public static void suis_pipeline() throws Exception {
        Pipeline pipeline = new Pipeline("suis_pipeline", inputFolder, baseOutputFolder);
        pipeline.addStage(new CrisprSelection(true, true, true), false);
        pipeline.addStage(new CrisprCommon(1, false, false, 1), false);

        SequenceEvaluator n1N20Eliminator = new MismatchEvaluator(null, Range.is(0), Range.between(Sequence.TARGET_INDEX_START, Sequence.N20_INDEX));
        pipeline.addStage(new CrisprElimination(Collections.singletonList(n1N20Eliminator)), false);

        SequenceEvaluator crisprEvaluator = new CrisprPamEvaluator(false);
        TypeEvaluator typeEvaluator = new TypeEvaluator(null,2,2,4,3);
        pipeline.addStage(new CandidateTyping(Collections.singletonList(crisprEvaluator),typeEvaluator), false);

        pipeline.addStage(new CandidateFeature(), false);
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

    public static void testPipelinePreprocessing() throws Exception {
        String inputFolder = baseInputFolder+"/test fasta splitting";
        Pipeline pipeline = new Pipeline("test fasta splitting", inputFolder, baseOutputFolder);
        CandidateTyping stage = new CandidateTyping();
        pipeline.addStage(stage);
        pipeline.preProcessStagesInput();
    }

    private static void setupLogging() throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s %5$s%6$s%n");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        mainLoggerFileHandler = new FileHandler(baseOutputFolder + "/application.log");
        mainLoggerFileHandler.setFormatter(simpleFormatter);
        log.addHandler(mainLoggerFileHandler);
    }
}
