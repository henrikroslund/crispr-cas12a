package com.henrikroslund.formats;

import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JakeCsv {

    private List<String> columnHeaders = new ArrayList<>();
    @Getter
    private List<List<String>> rows = new ArrayList<>();

    private static int PAM_COLUMN_INDEX = 8;
    private static int N1_N20_COLUMN_INDEX = 10;

    public JakeCsv(List<String[]> csv) {
        columnHeaders.addAll(Arrays.asList(csv.get(0)));
        columnHeaders.add("Matches in all genomes");
        columnHeaders.add("Number of matches");
        columnHeaders.add("Suis locations");
        columnHeaders.add("Suis Features");
        csv.remove(0);
        for(String[] row : csv) {
            List<String> columns = new ArrayList<>();
            for(int i=0; i<row.length; i++) {
                columns.add(row[i]);
            }
            rows.add(columns);
        }
    }

    public Sequence getRowSequence(int index) {
        List<String> row = rows.get(index);
        return new Sequence(row.get(PAM_COLUMN_INDEX) + row.get(N1_N20_COLUMN_INDEX),index, "");
    }

    public void addMatches(List<Sequence> sequences, int rowIndex, List<Sequence> suis, List<Feature> suisFeatures) {
        rows.get(rowIndex).add(sequencesToString(sequences));
        rows.get(rowIndex).add("" + sequences.size());
        rows.get(rowIndex).add(sequencesToString(suis));
        rows.get(rowIndex).add(featuresToString(suisFeatures));
    }

    public void writeToFile(String filename) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(filename));
        csvWriter.writeNext(columnHeaders.toArray(new String[0]));
        for(List<String> row : rows) {
            csvWriter.writeNext(row.toArray(new String[0]));
        }
        csvWriter.close();
    }

    private String featuresToString(List<Feature> features) {
        StringBuilder sequencesString = new StringBuilder();
        for(Feature feature: features) {
            sequencesString.append(feature.toString()).append("\n");
        }
        return sequencesString.toString();
    }

    private String sequencesToString(List<Sequence> sequences) {
        StringBuilder sequencesString = new StringBuilder();
        Collections.sort(sequences);
        for(Sequence sequence: sequences) {
            sequencesString.append(sequence.toString()).append("\n");
        }
        return sequencesString.toString();
    }
}
