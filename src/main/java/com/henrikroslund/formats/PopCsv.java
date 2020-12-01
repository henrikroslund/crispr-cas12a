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

public class PopCsv {

    private List<String> columnHeaders = new ArrayList<>();
    @Getter
    private List<List<String>> rows = new ArrayList<>();

    private static int PAM_COLUMN_INDEX = 8;
    private static int N1_N20_COLUMN_INDEX = 10;

    public PopCsv() {
        columnHeaders.add("Suis sequence");
        columnHeaders.add("Sequence count");
        columnHeaders.add("Strand");
        columnHeaders.add("Location");
        columnHeaders.add("GC Count");
        columnHeaders.add("Features");
    }

    public Sequence getRowSequence(int index) {
        List<String> row = rows.get(index);
        return new Sequence(row.get(PAM_COLUMN_INDEX) + row.get(N1_N20_COLUMN_INDEX),index, "");
    }

    public void addFeatureMatches(List<Sequence> suis, List<Feature> suisFeatures) {
        List<String> row = new ArrayList<>();
        row.addAll(sequencesToString(suis));
        row.add(featuresToString(suisFeatures));
        rows.add(row);
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

    private List<String> sequencesToString(List<Sequence> sequences) {
        Collections.sort(sequences);
        List<String> columns = new ArrayList<>();

        StringBuilder cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append(sequence.getRaw()).append("\n");
        }
        columns.add(cell.toString());

        columns.add(sequences.size() + "");

        cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append((sequence.getIsComplement() ? "-" : "+") + "\n");
        }
        columns.add(cell.toString());

        cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append(sequence.getStartIndex() + "\n");
        }
        columns.add(cell.toString());

        cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append(sequence.getGCCount() + "\n");
        }
        columns.add(cell.toString());

        return columns;
    }
}
