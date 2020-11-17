package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class ComparisonEvaluator implements SequenceEvaluator {

    public enum Type {
        TYPE_DISCARD,
        TYPE_1,
        TYPE_2,
        TYPE_3,
        TYPE_4
    }

    final Sequence sequence;
    final int minMismatches;
    final int maxMismatches;

    @Getter
    private Sequence match = null;
    @Getter
    private int mismatches = -1;
    @Getter
    private Type matchType = null;

    public ComparisonEvaluator(Sequence sequence, int minMismatches, int maxMismatches) {
        this.sequence = sequence;
        if(minMismatches > maxMismatches) {
            throw new IllegalArgumentException("minMatches should not be greater than maxMatches");
        }
        this.minMismatches = minMismatches;
        this.maxMismatches = maxMismatches;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMismatches = 0;

        // Check pam
        int pamMismatches = 0;
        for(int i=Sequence.PAM_INDEX_START; i<Sequence.PAM_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                pamMismatches++;
            }
        }

        // Check seed
        int seedMismatchesInARow = 0;
        int currentMismatches = 0;
        for(int i=Sequence.SEED_INDEX_START; i<Sequence.SEED_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                currentMismatches++;
            } else {
                currentMismatches = 0;
            }
            if(currentMismatches > seedMismatchesInARow) {
                seedMismatchesInARow = currentMismatches;
            }
        }

        // Check rest of the raw
        for(int i=Sequence.SEED_INDEX_END+1; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) == sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
            }
        }

        if(numberOfMismatches >= minMismatches && numberOfMismatches <= maxMismatches) {
            this.match = sequence;
            this.mismatches = numberOfMismatches;
            this.matchType = getType(pamMismatches, seedMismatchesInARow);
            return true;
        } else {
            this.mismatches = -1;
            this.match = null;
            this.matchType = null;
            return false;
        }
    }

    private Type getType(int pamMismatches, int seedMismatchesInARow) {
        Type result = null;
        if(pamMismatches >= 2) {
            result = Type.TYPE_1;
        }
        if(seedMismatchesInARow >= 2) {
            if(result == Type.TYPE_1) {
                result = Type.TYPE_3;
            } else {
                result = Type.TYPE_2;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "ComparisonEvaluator(" + minMismatches + "-" + maxMismatches + ") " +
                "matches(" + mismatches + ") " +
                "type(" + matchType.name() + ") " +
                match.toString();
    }
}
