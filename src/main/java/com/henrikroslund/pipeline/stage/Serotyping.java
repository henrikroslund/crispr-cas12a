package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.pcr.PcrProduct;
import com.henrikroslund.pcr.Serotype;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.Getter;
import lombok.extern.java.Log;
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
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder + "/" + "result.csv"));
        csvWriter.writeNext(new String[]{"Serotype", "Genome"});

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

                if(shortestDistancePrimerA == Integer.MAX_VALUE && shortestDistancePrimerB == Integer.MAX_VALUE) {
                    log.info("No match for " + serotype.getName() + " in genome " + file.getName());
                } else if(shortestDistancePrimerA < shortestDistancePrimerB) {
                    log.info("Found pcr product with distance " + shortestDistancePrimerA + " in genome " + file.getName());
                    pcrProducts.add(new PcrProduct(file.getName(), serotype, shortestDistancePrimerA, primerAPositions, primerBPositionsComplement));
                } else {
                    log.info("Found pcr product with distance " + shortestDistancePrimerB + " in genome " + file.getName());
                    pcrProducts.add(new PcrProduct(file.getName(), serotype, shortestDistancePrimerB, primerAPositionsComplement, primerBPositions));
                }
            });

            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
        }

        writeResults();

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
