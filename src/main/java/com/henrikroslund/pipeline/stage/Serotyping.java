package com.henrikroslund.pipeline.stage;

import com.henrikroslund.Genome;
import com.henrikroslund.Utils;
import com.henrikroslund.pcr.Serotype;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
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

    private final static Serotype serotype2 = new Serotype("TTAGCAACGTTGCCAATAAG", "AATCCTCCATTAAAACCCTG", "serotype2");
    private final static Serotype serotype14 = new Serotype("TTAGACAGACACCTTATAGG", "CTAGCTTCGTTACTTGATTC", "serotype14");
    private final static List<Serotype> serotypes = List.of(serotype2, serotype14);

    public Serotyping() {
        super(Serotyping.class);
    }

    @Override
    protected Genome execute(Genome inputGenome) throws Exception {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFolder + "/" + "result.csv"));
        csvWriter.writeNext(new String[]{"Serotype", "Genome"});

        List<File> genomeFiles = Utils.getFilesInFolder(inputFolder, FASTA_FILE_ENDING);
        int remainingFiles = genomeFiles.size();
        for(File file : genomeFiles) {
            Date startTime = new Date();

            Genome genome = new Genome(false, file.getName(), Utils.getFirstRow(file.getAbsolutePath()));
            String sequenceData = genome.getSequenceData();
            String sequenceDataComplement = Sequence.getComplement(sequenceData);

            serotypes.forEach(serotype -> {

                int shortestDistancePrimerA = getShortestDistance(
                        findOccurrences(sequenceData, serotype.getPrimerA()),
                        findOccurrences(sequenceDataComplement, serotype.getPrimerB()));

                /*
                if(primerAMatches.isEmpty() || primerBMatches.isEmpty()) {
                    log.info("No match for " + serotype.getName() + " in genome " + genome.getFilename());
                } else {
                    log.info("Found matches for " + serotype.getName());
                }
                 */
            });

            printProcessingTime(startTime);
            log.info("Files remaining: " + --remainingFiles + " / " + genomeFiles.size());
        }

        return inputGenome;
    }

    private int getShortestDistance(List<Integer> primerA, List<Integer> primerB) {
        AtomicInteger shortestDistance = new AtomicInteger(-1);
        if(primerA.isEmpty() || primerB.isEmpty()) {
            return shortestDistance.get();
        }
        primerA.forEach(primerAIndex -> {
            primerB.forEach(primerBIndex -> {
                int distance = Math.abs(primerAIndex - primerBIndex);
                if(shortestDistance.get() == -1 || distance < shortestDistance.get()) {
                    shortestDistance.set(distance);
                }
            });
        });
        return 0;
    }

    private void writeResults() {

    }

    private List<Integer> findOccurrences(String genomeSequenceData, String sequence) {
        List<Integer> indexes = new ArrayList<>();

        int index = 0;
        while(index != -1) {
            index = genomeSequenceData.indexOf(sequence, index + sequence.length());
            if (index != -1) {
                log.info("Found match at index " + index + " for sequence " + sequence);
                indexes.add(index);
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
