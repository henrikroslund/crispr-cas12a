package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class TypeEvaluator implements SequenceEvaluator {

    public enum Type {
        TYPE_1,
        TYPE_2,
        TYPE_3,
        TYPE_4,
        TYPE_5,
        TYPE_DISCARD
    }

    final Sequence sequence;

    @Getter
    private Sequence match = null;
    @Getter
    private int mismatches = -1;
    @Getter
    private Type matchType = null;
    @Getter
    private int mismatchesN7toN20 = 0;

    private final String[] matchRepresentation = new String[]{"?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?"};

    public TypeEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    private void reset() {
        match = null;
        mismatches = -1;
        matchType = null;
    }

    @Override
    public SequenceEvaluator clone() {
        return new TypeEvaluator(this.sequence);
    }

    // Will return true if the evaluation resulted in a matchType
    @Override
    public boolean evaluate(Sequence sequence) {
        reset();

        int numberOfMismatches = 0;

        // Check pam
        int pamWithoutVMismatches = 0;
        for(int i=Sequence.PAM_INDEX_START; i<Sequence.PAM_LENGTH-1; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                pamWithoutVMismatches++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                matchRepresentation[i] = MATCH_CHAR;
            }
        }

        // Check seed
        int seedMismatchesInARow = 0;
        int currentMismatches = 0;
        for(int i=Sequence.SEED_INDEX_START; i<=Sequence.SEED_INDEX_END; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                currentMismatches++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                currentMismatches = 0;
                matchRepresentation[i] = MATCH_CHAR;
            }
            if(currentMismatches > seedMismatchesInARow) {
                seedMismatchesInARow = currentMismatches;
            }
        }

        // Check rest of the raw
        for(int i=Sequence.SEED_INDEX_END+1; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                numberOfMismatches++;
                mismatchesN7toN20++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                matchRepresentation[i] = MATCH_CHAR;
            }
        }

        matchType = getType(pamWithoutVMismatches, seedMismatchesInARow);
        match = sequence;
        mismatches = numberOfMismatches;
        return matchType != null;
    }

    private Type getType(int pamMismatches, int seedMismatchesInARow) {
        Type result = mismatchesN7toN20 >= 3 ? Type.TYPE_5 : Type.TYPE_DISCARD;
        if(pamMismatches >= 2) {
            result = Type.TYPE_1;
        }
        if(seedMismatchesInARow >= 2) {
            result = result == Type.TYPE_1 ? Type.TYPE_3 : Type.TYPE_2;
        }
        return result;
    }

    @Override
    public String describe() {
        return "TypeEvaluator";
    }

    @Override
    public String toString() {
        if(match == null) {
            return describe() + " NO MATCH: " + sequence.toString();
        }
        if(matchType == null) {
            return describe() + " NO MATCH TYPE: " + sequence.toString();
        }
        return describe() + " " + matchType.name()
                + " ( " + SequenceEvaluator.toMatchRepresentation(matchRepresentation) + " ) "
                + "mismatchesN7To20: " + mismatchesN7toN20 + " " + match.toString();
    }
}
