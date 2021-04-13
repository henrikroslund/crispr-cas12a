package com.henrikroslund.configuration;

import com.henrikroslund.Genome;
import com.henrikroslund.configuration.stage.Stage;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

@Log
public class Pipeline {

    private List<Stage> stages = new ArrayList<>();

    private final String name;
    private final String inputFolder;
    private final String outputFolder;
    private final String outputInputFolder;

    public Pipeline(String name, String inputFolder, String outputFolder) {
        this.name = name;
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.outputInputFolder = outputFolder + "/input";
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void run() throws Exception {
        Genome stageResult = null;
        for(Stage stage : stages) {
            stageResult = stage.run(stageResult);
        }
    }

    public String toString() {
        return name;
    }
}