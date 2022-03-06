package com.henrikroslund.pipeline;

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
