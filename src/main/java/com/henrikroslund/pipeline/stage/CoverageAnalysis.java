package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.henrikroslund.Utils.*;

@Log
public class CoverageAnalysis extends Stage {

    public CoverageAnalysis() {
        super(CoverageAnalysis.class);
    }

    private final Map<Sequence, HashSet<String>> coverageMap = new ConcurrentHashMap<>();

    private void initiateCoverageMap(Genome genome) {
        for(Sequence sequence : genome.getSequences()) {
            coverageMap.put(sequence, new HashSet<>());
        }
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        initiateCoverageMap(inputGenome);

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, FASTA_FILE_ENDING);
        int remainingFiles = genomeFiles.size();
        for(File file : genomeFiles) {
            if(isChromosomeFile(file.getAbsolutePath()) && !isPrimaryChromosomeFile(file.getAbsolutePath())) {
                log.info("Will skip file because it is not primary chromosome " + file.getName());
                continue;
            }
            Date startTime = new Date();

            // We create only strict crispr sequences
            Genome genome = new Genome(file, Collections.singletonList(new CrisprPamEvaluator(true)), true, true);

            AtomicInteger counter = new AtomicInteger(0);
            inputGenome.getSequences().parallelStream().forEach(sequence -> {
                if(genome.exists(sequence)) {
                    coverageMap.get(sequence).add(file.getName());
                } else {
                    Sequence match = genome.getSequenceMatchingAllEvaluators(Collections.singletonList(
                            new MismatchEvaluator(sequence, Range.between(0, 1),
                                    Range.between(Sequence.SEED_INDEX_START, Sequence.N20_INDEX))));
                    if(match != null) {
                        log.info("Found approximate match for sequence " + sequence + " with sequence " + match);
                        coverageMap.get(sequence).add(file.getName());
                    }
                }
                counter.incrementAndGet();
                if (counter.get() % 1000 == 0) {
                    log.info(" Counter: " + counter + "/" + inputGenome.getSequences().size());
                }
            });
            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
        }

        writeResults(genomeFiles);

        return inputGenome;
    }

    private void writeResults(List<File> genomeFiles) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder + "/" + "result.csv"));
        csvWriter.writeNext(new String[]{"Name", "Sequence", "Strand", "Coverage %", "Coverage #", "Found in", "Not Found in"});

        int totalGenomes = genomeFiles.size();
        List<String> genomeFileNames = genomeFiles.stream().map(File::getName).collect(Collectors.toList());
        coverageMap.forEach((sequence, genomeMatches) -> {
            List<String> row = new ArrayList<>();
            row.add(sequence.getGenome());
            row.add(sequence.getRaw());
            row.add(sequence.getStrandRepresentation());
            double percent = 100.0 * ((double) genomeMatches.size() / (double) totalGenomes);
            row.add(String.format("%.2f", percent));
            row.add(genomeMatches.size() + " / " + totalGenomes);
            row.add(toCellWithNewline(genomeMatches));
            row.add(toCellWithNewline(genomeFileNames.stream().distinct().filter(filename -> !genomeMatches.contains(filename)).collect(Collectors.toSet())));
            csvWriter.writeNext(row.toArray(new String[0]));
        });
        csvWriter.close();
    }

    private String toCellWithNewline(Set<String> strings) {
        StringBuilder cellString = new StringBuilder();
        for(String string: strings) {
            cellString.append(string).append("\n");
        }
        return cellString.toString();
    }


    @Override
    public String toString() {
        return getName() + " " + getStageFolder();
    }

    @Override
    protected String getStageFolder() {
        return "/strains_coverage";
    }
}
