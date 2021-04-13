package com.henrikroslund.configuration.stage;

import com.henrikroslund.Genome;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Log
public abstract class Stage {

   @Getter(AccessLevel.PROTECTED)
   final private String name;

   @Getter(AccessLevel.PROTECTED)
   private static final String resultFilename = "result.sequences";

   protected final String inputFolder;
   protected final String outputFolder;
   protected final String outputInputFolder;

   private FileHandler logFileHandler;

    protected Stage(String name, String inputBaseFolder, String baseOutputFolder, String stageFolder) {
        this.name = name;
        this.inputFolder = inputBaseFolder + stageFolder;
        this.outputFolder = baseOutputFolder + "/" + name;
        new File(outputFolder).mkdirs();
        this.outputInputFolder = outputFolder + "/input";
    }

    protected void preExecute() throws Exception {
        Logger rootLogger = Logger.getLogger("");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        logFileHandler = new FileHandler(outputFolder + "/application.log");
        logFileHandler.setFormatter(simpleFormatter);
        rootLogger.addHandler(logFileHandler);
        log.info("Starting stage: " + name);
    }

    protected void postExecute() {
        log.info("Completed stage: " + name);
        Logger rootLogger = Logger.getLogger("");
        rootLogger.removeHandler(logFileHandler);
        logFileHandler.close();
    }

    public Genome run(Genome inputGenome) throws Exception {
        preExecute();
        Genome result = execute(inputGenome);
        result.writeSequences(outputFolder, getResultFilename(), "");
        postExecute();
        return result;
    }

    abstract protected Genome execute(Genome inputGenome) throws Exception;

    abstract public String toString();
}
