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
import com.henrikroslund.pcr.PcrProduct;
import com.henrikroslund.pcr.Serotype;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.henrikroslund.Utils.*;

@Log
public class Serotyping extends Stage {

    private final List<Serotype> serotypes;
    @Getter
    private final List<PcrProduct> pcrProducts;

    public Serotyping(List<Serotype> serotypes) {
        super(Serotyping.class);
        this.serotypes = serotypes;
        this.pcrProducts = new ArrayList<>();
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        List<String> columnHeaders = List.of("genome", "serotype", "primerA",
                "primerB", "distance", "primerAPositions", "primerBPositions");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder+"/result.csv"));
        csvWriter.writeNext(columnHeaders.toArray(new String[0]));

        // Used to write the names of the genomes where pcrs were found
        BufferedWriter genomeWriter = new BufferedWriter(new FileWriter(outputFolder + "/pcrGenomes.txt", true));

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, FASTA_FILE_ENDING);
        int remainingFiles = genomeFiles.size();
        for(File file : genomeFiles) {
            Date startTime = new Date();

            String sequenceData = Genome.getSequenceData(file.getAbsolutePath(), Utils.getFirstRow(file.getAbsolutePath()));
            String sequenceDataComplement = Sequence.getComplement(sequenceData);

            serotypes.forEach(serotype -> {

                List<Integer> primerAPositions = findOccurrences(sequenceData, serotype.getPrimerA(), false, file.getName());
                List<Integer> primerBPositionsComplement = findOccurrences(sequenceDataComplement, serotype.getPrimerB(), true, file.getName());
                int shortestDistancePrimerA = getShortestDistance(
                        primerAPositions, serotype.getPrimerA().length(),
                        primerBPositionsComplement, serotype.getPrimerB().length(), sequenceData.length());

                List<Integer> primerAPositionsComplement = findOccurrences(sequenceDataComplement, serotype.getPrimerA(),true, file.getName());
                List<Integer> primerBPositions = findOccurrences(sequenceData, serotype.getPrimerB(), false, file.getName());
                int shortestDistancePrimerB = getShortestDistance(
                        primerBPositions, serotype.getPrimerB().length(),
                        primerAPositionsComplement, serotype.getPrimerA().length(), sequenceData.length());

                PcrProduct pcrProduct = null;
                if(shortestDistancePrimerA == Integer.MAX_VALUE && shortestDistancePrimerB == Integer.MAX_VALUE) {
                    log.info("No match for " + serotype.getName() + " in genome " + file.getName());
                } else if(shortestDistancePrimerA < shortestDistancePrimerB) {
                    log.info("Found pcr product with distance " + shortestDistancePrimerA + " in genome " + file.getName());
                    pcrProduct = new PcrProduct(file.getName(), serotype, shortestDistancePrimerA, primerAPositions, primerBPositionsComplement);
                } else {
                    log.info("Found pcr product with distance " + shortestDistancePrimerB + " in genome " + file.getName());
                    pcrProduct = new PcrProduct(file.getName(), serotype, shortestDistancePrimerB, primerAPositionsComplement, primerBPositions);
                }
                if(pcrProduct != null) {
                    pcrProducts.add(pcrProduct);
                    csvWriter.writeNext(pcrToRow(pcrProduct).toArray(new String[0]));
                    try {
                        genomeWriter.append(pcrProduct.getGenome()).append("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            });

            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
        }

        csvWriter.close();
        genomeWriter.close();
        return inputGenome;
    }

    /**
     * Will calculate the shortest distance based on the
     */
    private int getShortestDistance(List<Integer> primerA, int primerALength, List<Integer> primerB, int primerBLength, int genomeSize) {
        AtomicInteger shortestDistance = new AtomicInteger(Integer.MAX_VALUE);
        if(primerA.isEmpty() || primerB.isEmpty()) {
            return shortestDistance.get();
        }
        primerA.forEach(primerAPosition -> {
            primerB.forEach(primerBPosition -> {
                int distance;
                if(primerAPosition > primerBPosition) {
                    distance = genomeSize - primerAPosition - primerALength + primerBPosition;
                } else {
                    distance = primerBPosition - primerAPosition + primerBLength;
                }
                if(shortestDistance.get() == Integer.MAX_VALUE || distance < shortestDistance.get()) {
                    shortestDistance.set(distance);
                }
            });
        });
        return shortestDistance.get();
    }

    private void writeResults() throws IOException {
        List<String> columnHeaders = List.of("genome", "serotype", "primerA",
                "primerB", "distance", "primerAPositions", "primerBPositions");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder+"/result.csv"));
        csvWriter.writeNext(columnHeaders.toArray(new String[0]));
        for(PcrProduct pcrProduct : pcrProducts) {
            List<String> columnValue = List.of(pcrProduct.getGenome(), pcrProduct.getSerotype().getName(),
                    pcrProduct.getSerotype().getPrimerA(), pcrProduct.getSerotype().getPrimerB(),
                    pcrProduct.getDistance()+"", toCellWithNewline(pcrProduct.getPrimerAPositions()),
                    toCellWithNewline(pcrProduct.getPrimerBPositions()));
            csvWriter.writeNext(columnValue.toArray(new String[0]));
        }
        csvWriter.close();
    }

    private List<String> pcrToRow(PcrProduct pcrProduct) {
        return List.of(pcrProduct.getGenome(), pcrProduct.getSerotype().getName(),
                pcrProduct.getSerotype().getPrimerA(), pcrProduct.getSerotype().getPrimerB(),
                pcrProduct.getDistance()+"", toCellWithNewline(pcrProduct.getPrimerAPositions()),
                toCellWithNewline(pcrProduct.getPrimerBPositions()));
    }

    private String toCellWithNewline(Collection<Integer> values) {
        StringBuilder cellString = new StringBuilder();
        for(Integer value : values) {
            cellString.append(value).append("\n");
        }
        return cellString.toString();
    }

    protected static List<Integer> findOccurrences(String genomeSequenceData, String sequence, boolean isComplement, String genomeName) {
        List<Integer> indexes = new ArrayList<>();

        int index = 0;
        while(index != -1) {
            index = genomeSequenceData.indexOf(sequence, index + sequence.length());
            if (index != -1) {
                log.info("Found match at index " + index + " for sequence " + sequence + " in genome " + genomeName);
                if(isComplement) {
                    indexes.add(genomeSequenceData.length() - index - sequence.length()+1);
                } else {
                    indexes.add(index+1);
                }
            }
        }
        return indexes;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append(getName());
        description.append(" ").append(getStageFolder());
        for(Serotype serotype : serotypes) {
            description.append(" ").append(serotype.toString());
        }
        return description.toString();
    }

    @Override
    protected String getStageFolder() {
        return "/strains_serotyping";
    }
}
