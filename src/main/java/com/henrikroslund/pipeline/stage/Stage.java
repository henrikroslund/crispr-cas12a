package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Main;
import com.henrikroslund.Utils;
import com.henrikroslund.sequence.Sequence;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

@Log
public abstract class Stage {

    abstract protected Genome execute(Genome inputGenome) throws Exception;
    abstract public String toString();
    abstract protected String getStageFolder();

    @Getter(AccessLevel.PROTECTED)
    final private String name;

    @Getter(AccessLevel.PROTECTED)
    private static final String resultFilename = "result";

    private static final Character FASTA_DELIMITER = '>';

    protected String inputFolder;
    protected String outputFolder;

    private FileHandler logFileHandler;

    private BufferedWriter discardWriter = null;
    private boolean shouldPreProcessFiles = true;

    protected Stage(Class<?> clazz) {
        this.name = clazz.getSimpleName();
    }

    public void configure(String inputBaseFolder, String baseOutputFolder, boolean shouldPreProcessFiles) {
        this.inputFolder = inputBaseFolder + getStageFolder();
        this.outputFolder = baseOutputFolder + "/" + name;
        File file = new File(outputFolder);
        if(file.mkdirs()) {
            log.info("Created directory: " + file.getAbsolutePath());
        }
        this.shouldPreProcessFiles = shouldPreProcessFiles;
    }

    protected void preExecute() throws Exception {
        Logger rootLogger = Logger.getLogger("");
        SimpleFormatter simpleFormatter = new SimpleFormatter();
        logFileHandler = new FileHandler(outputFolder + "/application.log");
        logFileHandler.setFormatter(simpleFormatter);
        rootLogger.addHandler(logFileHandler);
        // We remove the main logger for performance reasons
        rootLogger.removeHandler(Main.mainLoggerFileHandler);

        log.info("Starting stage: " + name);
        log.info(name + " configuration:\n" + this);
    }

    protected void postExecute() throws IOException {
        if(discardWriter != null) {
            discardWriter.close();
        }
        log.info("Completed stage: " + name);
        if(Main.mainLoggerFileHandler != null) {
            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(Main.mainLoggerFileHandler);
            rootLogger.removeHandler(logFileHandler);
            logFileHandler.close();
        }
    }

    public Genome run(Genome inputGenome) throws Exception {
        preExecute();
        Genome result = execute(inputGenome);
        if(result != null && result.getTotalSequences() > 0) {
            result.writeSequences(outputFolder, getResultFilename());
        }
        postExecute();
        return result;
    }

    protected BufferedWriter getDiscardWriter() throws IOException {
        if(discardWriter == null) {
            discardWriter = new BufferedWriter(new FileWriter(outputFolder + "/discarded.log", true));
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

    // Will do any required pre-processing of the input files.
    // Right now it will split fasta files with multiple genomes into separate files
    // and also make sure all characters are upper case.
    public void preProcessInputFiles() throws  Exception {
        if(!shouldPreProcessFiles) {
            log.info("Will skip preprocess files for stage " + getName());
            return;
        }
        List<File> fastaFiles = Utils.getFilesInFolder(inputFolder, Utils.FASTA_FILE_ENDING);
        log.info("Files to preprocess: " + fastaFiles.size());
        for(int i = 0; i < fastaFiles.size(); i++) {
            File fastaFile = fastaFiles.get(i);
            if(hasMultipleGenomesInFastaFile(fastaFile)) {
                log.info("Found multiple genomes in fasta file so will split file: " + fastaFile.getName());
                splitFastaWithMultipleGenomes(fastaFile);
            } else if(hasLowerCaseCharacters(fastaFile)) {
                log.info("Found lower case letter in fasta file so will create new file: " + fastaFile.getName());
                splitFastaWithMultipleGenomes(fastaFile);
            }
            if(i % 10 == 0) {
                log.info("Processed number of files: " + i);
            }
        }
    }

    private void splitFastaWithMultipleGenomes(File fastaFile) throws Exception {
        if(!StringUtils.endsWith(fastaFile.getName(), Utils.FASTA_FILE_ENDING)) {
            throw new Exception("Tried to split a non-fasta file");
        }
        BufferedWriter writer = null;
        Stream<String> lines = Files.lines(fastaFile.toPath());
        Iterator<String> it = lines.iterator();
        int fastaCount = 0;
        while(it.hasNext()) {
            String line = it.next();
            if(line.isEmpty()) {
                // We remove empty lines
                continue;
            } else if(line.charAt(0) == FASTA_DELIMITER) {
                if(writer != null) {
                    writer.flush();
                    writer.close();
                }
                if(Utils.isChromosomeFile(fastaFile.getName())) {
                    throw new Exception("No support for splitting chromosome files at this time!");
                }
                String output = inputFolder + "/" + FilenameUtils.removeExtension(fastaFile.getName()) + "_" + fastaCount + Utils.FASTA_FILE_ENDING;
                if(new File(output).exists()) {
                    throw new Exception("Did not expect file to already exist while splitting fasta file: " + output);
                }
                writer = new BufferedWriter(new FileWriter(output, true));
                fastaCount++;
            } else {
                // We modify everything to be upper case except the first line
                line = line.toUpperCase();
            }
            if(writer == null) {
                throw new Exception("Something went wrong trying to split fasta file");
            }
            writer.write(line);
            writer.newLine();
        }
        if(writer != null) {
            writer.flush();
            writer.close();
        }
        File renameFile = new File(fastaFile.getAbsolutePath()+".skip");
        if(!fastaFile.renameTo(renameFile)) {
            throw new Exception("Unable to rename original file: " + fastaFile.getName() + " to " + renameFile.getName());
        }
    }

    private boolean hasMultipleGenomesInFastaFile(File fastaFile) throws IOException {
        try (Stream<String> lines = Files.lines(fastaFile.toPath())) {
            int numberOfFasta = (int) lines.parallel().filter(s -> s.isEmpty() || s.charAt(0) == FASTA_DELIMITER).count();
            if (numberOfFasta > 1) {
                log.info("Found " + numberOfFasta + " genomes inside fasta file: " + fastaFile.getName());
                return true;
            }
            return false;
        }
    }

    private boolean hasLowerCaseCharacters(File fastaFile) throws IOException {
        Stream<String> lines = Files.lines(fastaFile.toPath());
        int linesWithLowerCase = (int) lines.parallel().filter(s -> !StringUtils.isAllUpperCase(s)).count();
        if(linesWithLowerCase > 1) {
            log.info("Found lower case letters in file: " + fastaFile.getName());
            return true;
        }
        return false;
    }

}
