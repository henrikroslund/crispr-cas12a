package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

public class MatchEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private final Range<Integer> range;

    @Getter
    private Sequence match = null;
    @Getter
    private int matches = -1;

    public MatchEvaluator(Sequence sequence, Range<Integer> range) {
        this.range = range;
        this.sequence = sequence;
    }

    @Override
    public SequenceEvaluator clone() {
        return new MatchEvaluator(this.sequence, this.range);
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMatches = 0;

        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) == sequence.getRaw().charAt(i)) {
                numberOfMatches++;
            }
        }

        if(range.contains(numberOfMatches)) {
            this.match = sequence;
            this.matches = numberOfMatches;
            return true;
        } else {
            this.matches = -1;
            this.match = null;
            return false;
        }
    }

    @Override
    public String describe() {
        return "MatchEvaluator(" + range + ")";
    }

    @Override
    public String toString() {
        return describe() + " matches(" + matches + ") " + match.toString();
    }
}
