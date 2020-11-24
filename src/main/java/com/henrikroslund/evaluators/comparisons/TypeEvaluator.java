package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class TypeEvaluator implements SequenceEvaluator {

    public enum Type {
        TYPE_DISCARD_A,
        TYPE_DISCARD_B,
        TYPE_1,
        TYPE_2,
        TYPE_3,
        TYPE_4
    }

    final Sequence sequence;

    @Getter
    private Sequence match = null;
    @Getter
    private int mismatches = -1;
    @Getter
    private Type matchType = null;

    public TypeEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    private void reset() {
        match = null;
        mismatches = -1;
        matchType = null;
    }

    public boolean isDiscardType() {
        return matchType == Type.TYPE_DISCARD_A || matchType == Type.TYPE_DISCARD_B;
    }

    // Will return true if the evaluation resulted in a matchType
    @Override
    public boolean evaluate(Sequence sequence) {
        reset();

        // Check complete match
        if(sequence.equals(this.sequence)) {
            match = sequence;
            mismatches = 0;
            matchType = Type.TYPE_DISCARD_A;
        }

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
        for(int i=Sequence.SEED_INDEX_START; i<Sequence.SEED_INDEX_END; i++) {
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
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
            }
        }

        matchType = getType(pamMismatches, seedMismatchesInARow, numberOfMismatches);
        match = sequence;
        mismatches = numberOfMismatches;
        return matchType != null;
    }

    private Type getType(int pamMismatches, int seedMismatchesInARow, int totalMismatches) {
        if(totalMismatches <= 1) {
            return Type.TYPE_DISCARD_A;
        }

        if(totalMismatches >= 12) {
            return Type.TYPE_4;
        }

        Type result = Type.TYPE_DISCARD_B;
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
        if(match == null) {
            return "NO MATCH: " + sequence.toString();
        }
        if(matchType == null) {
            return "NO MATCH TYPE: " + sequence.toString();
        }
        return matchType.name() + " " + match.toString();
    }
}
