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
    private final String describeIndexesToCompare;
    private final boolean[] indexesToCompare = new boolean[Sequence.RAW_LENGTH];
    private final List<Range<Integer>> rangeIndexesToCompare;

    @Getter
    private Sequence match = null;
    private int mismatches = -1;
    private final String[] matchRepresentation = new String[]{"?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?"};

    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange, List<Range<Integer>> rangeIndexesToCompare) {
        this.sequence = sequence;
        this.mismatchRange = mismatchRange;
        this.rangeIndexesToCompare = rangeIndexesToCompare;

        Iterator<Range<Integer>> it = rangeIndexesToCompare.iterator();
        StringBuilder describeIndexes = new StringBuilder();
        while(it.hasNext()) {
            Range<Integer> range = it.next();
            for(int i = 0; i<indexesToCompare.length; i++) {
                if(range.contains(i)) {
                    indexesToCompare[i] = true;
                }
            }
            describeIndexes.append(range);
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
            if(indexesToCompare[i]) {
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
        return new MismatchEvaluator(sequence, mismatchRange, rangeIndexesToCompare);
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
