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
    private String[] matchRepresentation = new String[]{"????????????????????????"};

    public MismatchEvaluator(Sequence sequence, Range<Integer> mismatchRange) {
        this.mismatchRange = mismatchRange;
        this.sequence = sequence;
    }

    @Override
    public SequenceEvaluator clone() {
        return new MismatchEvaluator(this.sequence, this.mismatchRange);
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMismatches = 0;

        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                matchRepresentation[i] = MATCH_CHAR;
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
        return describe() + " mismatches: " + mismatches + " ( "
                + SequenceEvaluator.toMatchRepresentation(matchRepresentation) +  " ) " + match.toString();
    }
}
