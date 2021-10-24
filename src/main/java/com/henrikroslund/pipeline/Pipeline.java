package com.henrikroslund.pipeline;

import com.henrikroslund.Genome;
import com.henrikroslund.pipeline.stage.Stage;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

@Log
public class Pipeline {

    private final List<Stage> stages = new ArrayList<>();

    private final String name;
    private final String inputFolder;
    private final String outputFolder;

    public Pipeline(String name, String inputFolder, String outputFolder) {
        this.name = name;
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
    }

    public void addStage(Stage stage, boolean shouldPreProcessFiles) {
        stage.configure(inputFolder, outputFolder, shouldPreProcessFiles);
        stages.add(stage);
    }

    public void addStage(Stage stage) {
        addStage(stage, true);
    }

    public void preProcessStagesInput() throws Exception {
        log.info("Will pre-process all stages input files.");
        for(Stage stage : stages) {
            stage.preProcessInputFiles();
        }
    }

    public void run() throws Exception {
        log.info("Starting pipeline: " + name);
        log.info("Pipeline input folder " + inputFolder);
        log.info("Pipeline output folder " + outputFolder);

        // First we print the pipeline
        StringBuilder description = new StringBuilder("Printing pipeline stages and configurations:");
        for(Stage stage : stages) {
            description.append("\n").append(stage);
        }
        log.info(description.toString());
        log.info("Will start pipeline.");

        preProcessStagesInput();

        Genome stageResult = null;
        for(Stage stage : stages) {
            stageResult = stage.run(stageResult);
        }

        log.info("Finished pipeline: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}