package com.henrikroslund.genomeFeature;

import com.henrikroslund.sequence.Sequence;
import lombok.extern.java.Log;

import java.util.List;

@Log
public class Feature {
    private final int startIndex;
    private final int endIndex;
    private final boolean isComplement;
    private final String type;
    private final List<String> features;

    public Feature(int startIndex, int endIndex, String type, List<String> features) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.isComplement = startIndex > endIndex;
        this.type = type;
        this.features = features;
    }

    public boolean isMatch(Sequence sequence, boolean mustMatchStrand) {
        if(mustMatchStrand && sequence.getIsComplement() != isComplement) {
            return false;
        }
        return sequence.getStartIndex() <= endIndex && sequence.getEndIndex() >= startIndex;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                startIndex + " " + endIndex + " " + (isComplement ? "-" : "+") + " " + type + " \n");
        for(String feature : features) {
            result.append(feature).append("\n");
        }
        return result.toString();
    }
}
