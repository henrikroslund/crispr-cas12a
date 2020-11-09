package com.henrikroslund.genomeFeature;

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

    String genomeFeature;
    List<Feature> features = new ArrayList<>();

    public GenomeFeature(File file) throws Exception {
        Path filePath = Path.of(file.getAbsolutePath());
        Stream<String> lines = Files.lines(filePath);
        Iterator<String> it = lines.iterator();
        genomeFeature = it.next();

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
                start = Integer.parseInt(parts[0]);
                end = Integer.parseInt(parts[1]);
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

    public List<Feature> getMatchingFeatures(List<Sequence> sequences) {
        List<Feature> matches = new ArrayList<>();
        for(Feature feature : features) {
            for(Sequence sequence : sequences) {
                if(feature.isMatch(sequence)) {
                    matches.add(feature);
                    break;
                }
            }
        }
        return  matches;
    }
}
