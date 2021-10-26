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
import java.util.ArrayList;
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

                int shortestDistancePrimerA = getShortestDistance(
                        findOccurrences(sequenceData, serotype.getPrimerA(), false), serotype.getPrimerA().length(),
                        findOccurrences(sequenceDataComplement, serotype.getPrimerB(), true), serotype.getPrimerB().length(), sequenceData.length());

                int shortestDistancePrimerB = getShortestDistance(
                        findOccurrences(sequenceData, serotype.getPrimerB(), false), serotype.getPrimerB().length(),
                        findOccurrences(sequenceDataComplement, serotype.getPrimerA(),true), serotype.getPrimerA().length(), sequenceData.length());

                int shortestDistance = shortestDistancePrimerA;
                if(shortestDistance == -1 || (shortestDistancePrimerB != -1 && shortestDistancePrimerB < shortestDistance)) {
                    shortestDistance = shortestDistancePrimerB;
                }

                if(shortestDistance != -1) {
                    log.info("Found pcr product with distance " + shortestDistance + " in genome " + file.getName());
                    pcrProducts.add(new PcrProduct(file.getName(), serotype, shortestDistance));
                } else {
                    log.info("No match for " + serotype.getName() + " in genome " + file.getName());
                }
            });

            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
        }

        return inputGenome;
    }

    private int getShortestDistance(List<Integer> primerA, int primerALength, List<Integer> primerB, int primerBLength, int genomeSize) {
        AtomicInteger shortestDistance = new AtomicInteger(-1);
        if(primerA.isEmpty() || primerB.isEmpty()) {
            return shortestDistance.get();
        }
        primerA.forEach(primerAIndex -> {
            primerB.forEach(primerBIndex -> {
                int distance;
                if(primerAIndex > primerBIndex) {
                    distance = genomeSize - primerAIndex - primerALength + primerBIndex;
                } else {
                    distance = primerBIndex - primerAIndex + primerBLength;
                }
                if(shortestDistance.get() == -1 || distance < shortestDistance.get()) {
                    shortestDistance.set(distance);
                }
            });
        });
        return shortestDistance.get();
    }

    private void writeResults() {

    }

    protected static List<Integer> findOccurrences(String genomeSequenceData, String sequence, boolean isComplement) {
        List<Integer> indexes = new ArrayList<>();

        int index = 0;
        while(index != -1) {
            index = genomeSequenceData.indexOf(sequence, index + sequence.length());
            if (index != -1) {
                log.info("Found match at index " + index + " for sequence " + sequence);
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
