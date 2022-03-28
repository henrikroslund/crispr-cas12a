package com.henrikroslund.genomeFeature;

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

import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Log
public class GenomeFeature {

    private final List<Feature> features = new ArrayList<>();

    public GenomeFeature(File file) throws Exception {
        log.info("Creating GenomeFeature from file " + file.getName());
        Path filePath = Path.of(file.getAbsolutePath());
        Stream<String> lines = Files.lines(filePath);
        Iterator<String> it = lines.iterator();

        // The first line is just the name so we ignore
        it.next();

        int start = -1;
        int end = -1;
        String type = "";
        List<String> featureLines = new ArrayList<>();

        while(it.hasNext()) {
            String line = it.next();
            // Last lines could be empty
            if(line.isEmpty()) {
                continue;
            }
            boolean isNewFeature = isNewFeature(line);
            if(isNewFeature) {
                if(start != -1) {
                    features.add(new Feature(start, end, type, featureLines));
                }
                featureLines = new ArrayList<>();
                String[] parts = line.split("\t");
                if(parts.length < 2) {
                    throw new Exception("Unexpected amount of parts " + parts.length + " for: " + line);
                }
                start = Integer.parseInt(parts[0].replaceAll("[<>]", ""));
                end = Integer.parseInt(parts[1].replaceAll("[<>]", ""));
                type = "";
                if(parts.length >= 3) {
                    type = parts[2];
                }
            } else {
                featureLines.add(line);
            }
        }
    }

    private boolean isNewFeature(String line) {
        return !line.startsWith("\t");
    }

    public List<Feature> getMatchingFeatures(List<Sequence> sequences, boolean mustMatchStrand) {
        List<Feature> matches = new ArrayList<>();
        for(Feature feature : features) {
            for(Sequence sequence : sequences) {
                if(feature.isMatch(sequence, mustMatchStrand)) {
                    matches.add(feature);
                    break;
                }
            }
        }
        return  matches;
    }
}
