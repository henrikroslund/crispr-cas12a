package com.henrikroslund.configuration;

import com.henrikroslund.Genome;
import com.henrikroslund.configuration.stage.Stage;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

@Log
public class Pipeline {

    // TODO print pipeline and stage configuration in log

    private final List<Stage> stages = new ArrayList<>();

    private final String name;
    private final String inputFolder;
    private final String outputFolder;

    public Pipeline(String name, String inputFolder, String outputFolder) {
        this.name = name;
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
    }

    public void addStage(Stage stage) {
        stage.configure(inputFolder, outputFolder);
        stages.add(stage);
    }

    public void run() throws Exception {
        log.info("Starting pipeline: " + name);

        Genome stageResult = null;
        for(Stage stage : stages) {
            stageResult = stage.run(stageResult);
        }

        log.info("Finished pipeline: " + name);
    }

    public String toString() {
        return name;
    }
}