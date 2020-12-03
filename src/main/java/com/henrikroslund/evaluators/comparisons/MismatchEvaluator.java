package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

public class MismatchEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private final Range<Integer> mismatchRange;

    @Getter
    private Sequence match = null;
    @Getter
    private int mismatches = -1;

    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange) {
        this.mismatchRange = mismatchRange;
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMismatches = 0;

        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
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
    public String describe() {
        return "MismatchEvaluator(" + mismatchRange + ")";
    }

    @Override
    public String toString() {
        return "MismatchEvaluator(" + mismatchRange + ") " +
                "matches(" + mismatches + ") " +
                match.toString();
    }
}
