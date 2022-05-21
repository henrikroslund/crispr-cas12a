package com.henrikroslund.pipeline.stage;

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
import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.comparisons.MismatchEvaluator;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.henrikroslund.Utils.*;

/**
 * This stage will analyze the coverage of input genome sequences with
 * all genome files in the input folder.
 * The coverage results are saved to a csv file .
 */

@Log
public class CoverageAnalysis extends Stage {

    public CoverageAnalysis() {
        super(CoverageAnalysis.class);
    }

    protected static Map<Sequence, TreeSet<String>> initiateCoverageMap(Genome genome) {
        Map<Sequence, TreeSet<String>> coverageMap = Collections.synchronizedMap(new TreeMap<>());
        for(Sequence sequence : genome.getSequences()) {
            coverageMap.put(sequence, new TreeSet<>());
        }
        return coverageMap;
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        // After the analysis this map will contain all the filenames each sequence was found in.
        Map<Sequence, TreeSet<String>> coverageMap = initiateCoverageMap(inputGenome);

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

        writeResults(genomeFiles, coverageMap);

        return inputGenome;
    }

    private void writeResults(List<File> genomeFiles, Map<Sequence, TreeSet<String>> coverageMap) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder + "/" + "result.csv"));
        csvWriter.writeNext(new String[]{"Name", "Sequence", "Strand", "Coverage %", "Coverage #", "Found in", "Not Found in"});

        int totalGenomes = genomeFiles.size();
        List<String> genomeFileNames = genomeFiles.stream().map(File::getName).collect(Collectors.toList());
        // Write single sequence results
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

        // Write dual sequences results
        getCoverageMapMultipleSequences(coverageMap).forEach(entry -> {
            List<String> row = new ArrayList<>();
            List<Sequence> sequences = entry.getLeft();
            HashSet<String> genomeMatches = entry.getRight();
            row.add(toCellWithNewline(sequences.stream().map(Sequence::getGenome).collect(Collectors.toList())));
            row.add(toCellWithNewline(sequences.stream().map(Sequence::getRaw).collect(Collectors.toList())));
            row.add(toCellWithNewline(sequences.stream().map(Sequence::getStrandRepresentation).collect(Collectors.toList())));
            double percent = 100.0 * ((double) genomeMatches.size() / (double) totalGenomes);
            row.add(String.format("%.2f", percent));
            row.add(genomeMatches.size() + " / " + totalGenomes);
            row.add(toCellWithNewline(genomeMatches));
            row.add(toCellWithNewline(genomeFileNames.stream().distinct().filter(filename -> !genomeMatches.contains(filename)).collect(Collectors.toSet())));
            csvWriter.writeNext(row.toArray(new String[0]));
        });
        csvWriter.close();
    }

    protected static TreeSet<Pair<List<Sequence>, HashSet<String>>> getCoverageMapMultipleSequences(Map<Sequence, TreeSet<String>> coverage) {
        TreeSet<Pair<List<Sequence>, HashSet<String>>> result = new TreeSet<>((o1, o2) -> {
            if(o1.getLeft().size() > o2.getLeft().size()) {
                return 1;
            } else if(o1.getLeft().size() < o2.getLeft().size()) {
                return -1;
            }
            for(int i=0; i<o1.getLeft().size(); i++) {
                int compareTo = o1.getLeft().get(i).compareTo(o2.getLeft().get(i));
                if(compareTo != 0) {
                    return compareTo;
                }
            }
            return 0;
        });
        coverage.forEach((sequence, genomeMatches) -> {
            coverage.forEach((sequence2, genomeMatches2) -> {
                if(sequence == sequence2) {
                    return;
                }
                List<Sequence> sequences = new ArrayList<>();
                sequences.add(sequence);
                sequences.add(sequence2);
                // We sort to avoid entries like {X,Y} and {Y,X}
                Collections.sort(sequences);
                HashSet<String> combinedGenomeMatches = new HashSet<>();
                combinedGenomeMatches.addAll(genomeMatches);
                combinedGenomeMatches.addAll(genomeMatches2);
                result.add(Pair.of(sequences, combinedGenomeMatches));
            });
        });
        return result;
    }

    private String toCellWithNewline(Collection<String> strings) {
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
