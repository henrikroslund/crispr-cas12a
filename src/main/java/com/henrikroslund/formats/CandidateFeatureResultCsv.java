package com.henrikroslund.formats;

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

import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.genomeFeature.Feature;
import com.henrikroslund.sequence.Sequence;
import com.opencsv.CSVWriter;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CandidateFeatureResultCsv {

    private final List<String> columnHeaders = new ArrayList<>();
    @Getter
    private final List<List<String>> rows = new ArrayList<>();

    public CandidateFeatureResultCsv() {
        columnHeaders.add("Suis sequence");
        columnHeaders.add("Sequence count");
        columnHeaders.add("Strand");
        columnHeaders.add("Location");
        columnHeaders.add("GC Count");
        for(TypeEvaluator.Type type : TypeEvaluator.Type.values()) {
            columnHeaders.add(type.name());
        }
        columnHeaders.add("Found in reference");
        columnHeaders.add("Features");
    }

    public void addFeatureMatches(List<Sequence> suis, List<Feature> features, boolean foundInReferenceGenome) {
        List<String> row = new ArrayList<>(sequencesToString(suis));
        row.add(foundInReferenceGenome ? "Yes" : "No");
        row.add(featuresToString(features));
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
            cell.append(sequence.getIsComplement() ? "-" : "+").append("\n");
        }
        columns.add(cell.toString());

        cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append(sequence.getStartIndex()).append("\n");
        }
        columns.add(cell.toString());

        cell = new StringBuilder();
        for(Sequence sequence: sequences) {
            cell.append(sequence.getGCCount()).append("\n");
        }
        columns.add(cell.toString());

        for(TypeEvaluator.Type type : TypeEvaluator.Type.values()) {
            cell = new StringBuilder();
            for(Sequence sequence: sequences) {
                cell.append(sequence.getMetaData().get(type)).append("\n");
            }
            columns.add(cell.toString());
        }

        return columns;
    }
}
