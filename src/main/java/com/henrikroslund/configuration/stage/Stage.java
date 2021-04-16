package com.henrikroslund.configuration.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.sequence.Sequence;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Log
public abstract class Stage {

    abstract protected Genome execute(Genome inputGenome) throws Exception;
    abstract public String toString();
    abstract protected String getStageFolder();

    @Getter(AccessLevel.PROTECTED)
    final private String name;

    @Getter(AccessLevel.PROTECTED)
    private static final String resultFilename = "result.sequences";

    protected String inputFolder;
    protected String outputFolder;
    protected String outputInputFolder;

    private FileHandler logFileHandler;

    private BufferedWriter discardWriter = null;

    protected Stage(Class clazz) {
        this.name = clazz.getSimpleName();
    }

    public void configure(String inputBaseFolder, String baseOutputFolder) {
        this.inputFolder = inputBaseFolder + getStageFolder();
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
        log.info(name + " configuration:\n" + this);
    }

    protected void postExecute() throws IOException {
        if(discardWriter != null) {
            discardWriter.close();
        }
        log.info("Completed stage: " + name);
        Logger rootLogger = Logger.getLogger("");
        rootLogger.removeHandler(logFileHandler);
        logFileHandler.close();
    }

    public Genome run(Genome inputGenome) throws Exception {
        preExecute();
        Genome result = execute(inputGenome);
        if(result != null && result.getTotalSequences() > 0) {
            result.writeSequences(outputFolder, getResultFilename(), "");
        }
        postExecute();
        return result;
    }

    protected BufferedWriter getDiscardWriter() throws IOException {
        if(discardWriter == null) {
            discardWriter = new BufferedWriter(new FileWriter(outputFolder + "/discarded.sequences", true));
        }
        return discardWriter;
    }

    protected void writeDiscarded(Collection<Sequence> discardedSequences, String message) throws IOException {
        for(Sequence sequence : discardedSequences) {
            getDiscardWriter().append(sequence.toString()).append(message).append("\n");
        }

    }

    protected void printProcessingTime(Date startTime) {
        long durationSeconds = (new Date().getTime() - startTime.getTime())/1000;
        if(durationSeconds > 30) {
            log.info("Finished processing in " + durationSeconds + " seconds");
        }
    }

}
