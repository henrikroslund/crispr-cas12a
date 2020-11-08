package com.henrikroslund.formats;

import com.henrikroslund.Sequence;
import com.opencsv.CSVWriter;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JakeCsv {

    private List<String> columnHeaders = new ArrayList<>();
    @Getter
    private List<List<String>> rows = new ArrayList<>();

    private static int PAM_COLUMN_INDEX = 8;
    private static int N1_N20_COLUMN_INDEX = 10;
    private static int MATCHES_INDEX = 11;

    public JakeCsv(List<String[]> csv) {
        columnHeaders.addAll(Arrays.asList(csv.get(0)));
        columnHeaders.add("Matches in all genomes");
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

    public void addMatches(List<Sequence> sequences, int rowIndex) {
        String result = "";
        for(Sequence sequence: sequences) {
            result += sequence.toString() + "\n";
        }
        rows.get(rowIndex).add(result);
    }

    public void writeToFile(String filename) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(filename));
        csvWriter.writeNext(columnHeaders.toArray(new String[0]));
        for(List<String> row : rows) {
            csvWriter.writeNext(row.toArray(new String[0]));
        }
        csvWriter.close();
    }
}
