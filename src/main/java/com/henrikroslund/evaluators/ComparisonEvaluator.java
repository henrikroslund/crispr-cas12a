package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class ComparisonEvaluator implements SequenceEvaluator {

    final Sequence sequence;
    final int minMatches;
    final int maxMatches;

    @Getter
    private Sequence match = null;
    @Getter
    private int matches = -1;

    public ComparisonEvaluator(Sequence sequence, int minMatches, int maxMatches) {
        this.sequence = sequence;
        if(minMatches > maxMatches) {
            throw new IllegalArgumentException("minMatches should not be greater than maxMatches");
        }
        this.minMatches = minMatches;
        this.maxMatches = maxMatches;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMatches = 0;
        for(int i=0; i<sequence.getRaw().length(); i++) {
            if(this.sequence.getRaw().charAt(i) == sequence.getRaw().charAt(i)) {
                numberOfMatches++;
            }
        }
        if(numberOfMatches >= minMatches && numberOfMatches <= maxMatches) {
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
    public String toString() {
        return "ComparisonEvaluator(" + minMatches + "-" + maxMatches + ") matches(" + matches + ") " + match.toString();
    }
}
