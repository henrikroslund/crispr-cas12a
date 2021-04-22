package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MismatchEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    private final Range<Integer> mismatchRange;
    private final List<Range<Integer>> indexesToCompare;
    private final String describeIndexesToCompare;

    @Getter
    private Sequence match = null;
    private int mismatches = -1;
    private final String[] matchRepresentation = new String[]{"?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?"};

    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange, List<Range<Integer>> indexesToCompare) {
        this.sequence = sequence;
        this.mismatchRange = mismatchRange;
        this.indexesToCompare = indexesToCompare;
        Iterator<Range<Integer>> it = indexesToCompare.iterator();
        StringBuilder describeIndexes = new StringBuilder();
        while(it.hasNext()) {
            describeIndexes.append(it.next());
            if(it.hasNext()) {
                describeIndexes.append(", ");
            }
        }
        describeIndexesToCompare = describeIndexes.toString();
    }
    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange, Range<Integer> indexesToCompare) {
        this(sequence, mismatchRange, Collections.singletonList(indexesToCompare));
    }


    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange) {
        this(sequence, mismatchRange, Range.between(0,24));
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMismatches = 0;
        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            int finalI = i;
            boolean inRange = indexesToCompare.stream().anyMatch(integerRange -> integerRange.contains(finalI));
            if(inRange) {
                if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                    numberOfMismatches++;
                    matchRepresentation[i] = MISMATCH_CHAR;
                } else {
                    matchRepresentation[i] = MATCH_CHAR;
                }
            }
        }

        if(mismatchRange.contains(numberOfMismatches)) {
            this.match = sequence;
            this.mismatches = numberOfMismatches;
            return true;
        } else {
            this.mismatches = -1;
            this.match = null;
            return false;
        }
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new MismatchEvaluator(sequence, mismatchRange, indexesToCompare);
    }

    @Override
    public String describe() {
        return "MismatchEvaluator(mismatches: " + mismatchRange + " indexes: " + describeIndexesToCompare + " )";
    }

    @Override
    public String toString() {
        return describe() + " mismatches: " + mismatches + " ( "
                + SequenceEvaluator.toMatchRepresentation(matchRepresentation) +  " ) " + (match != null ? match.toString() : "");
    }
}
