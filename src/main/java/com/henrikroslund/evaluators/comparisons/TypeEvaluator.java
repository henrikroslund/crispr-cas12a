package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeEvaluator implements SequenceEvaluator {

    public enum Type {
        TYPE_1, // >=X mismatches in PAM
        TYPE_2, // >=X consecutive mismatches in Seed
        TYPE_3, // If Type_1 & Type_2
        TYPE_4, // Did not bind in genome. Cannot be evaluated in this class on a sequence comparison level.
        TYPE_5, // >=X mismatches in N7-N20
        TYPE_DISCARD // If no other type applies
    }

    private static final int type1Criteria = 2;
    private static final int type2Criteria = 2;
    private static final int type5Criteria = 3;

    final Sequence sequence;

    @Getter
    private Sequence match = null;
    @Getter
    private final List<Type> matchTypes = new ArrayList<>();
    private final String[] matchRepresentation = new String[]{"?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?"};

    public TypeEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    private void reset() {
        match = null;
        matchTypes.clear();
    }

    // Will return true if the evaluation resulted in a matchType
    @Override
    public boolean evaluate(Sequence sequence) {
        reset();

        // Check pam
        int pamWithoutVMismatches = 0;
        for(int i=Sequence.PAM_INDEX_START; i<Sequence.PAM_LENGTH-1; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                pamWithoutVMismatches++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                matchRepresentation[i] = MATCH_CHAR;
            }
        }

        // Check seed
        int seedMismatchesInARow = 0;
        int seedMismatches = 0;
        int currentMismatches = 0;
        for(int i=Sequence.SEED_INDEX_START; i<=Sequence.SEED_INDEX_END; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                currentMismatches++;
                seedMismatches++;
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
        int mismatchesN7toN20 = 0;
        for(int i=Sequence.SEED_INDEX_END+1; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) != sequence.getRaw().charAt(i)) {
                mismatchesN7toN20++;
                matchRepresentation[i] = MISMATCH_CHAR;
            } else {
                matchRepresentation[i] = MATCH_CHAR;
            }
        }

        evaluateTypes(pamWithoutVMismatches, seedMismatches, seedMismatchesInARow, mismatchesN7toN20);
        match = sequence;
        return true; // TODO is this good?
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new TypeEvaluator(sequence);
    }

    private void evaluateTypes(int pamMismatches, int seedMismatches, int seedMismatchesInARow, int mismatchesN7toN20) {
        // TODO add the different types to be turned on/off
        if(pamMismatches >= type1Criteria) {
            matchTypes.add(Type.TYPE_1);
        }
        if(seedMismatchesInARow >= type2Criteria) {
            matchTypes.add(Type.TYPE_2);
        }
        if(matchTypes.containsAll(Arrays.asList(Type.TYPE_1, Type.TYPE_2))) {
            matchTypes.add(Type.TYPE_3);
        }
        if(mismatchesN7toN20 >= type5Criteria) {
            matchTypes.add(Type.TYPE_5);
        }
        if(seedMismatches > 3)
        if(matchTypes.isEmpty()) {
            matchTypes.add(Type.TYPE_DISCARD);
        }
    }

    @Override
    public String describe() {
        return "TypeEvaluator( "
                + Type.TYPE_1.name() + ": " + type1Criteria + " "
                + Type.TYPE_2.name() + ": " + type2Criteria + " "
                + Type.TYPE_5.name() + ": " + type5Criteria + " "
                + ")";
    }

    @Override
    public String toString() {
        if(match == null) {
            return describe() + " NO MATCH: " + sequence.toString();
        }
        if(matchTypes.isEmpty()) {
            return describe() + " NO MATCH TYPE: " + sequence.toString();
        }
        return describe() + " " + matchTypes
                + " ( " + SequenceEvaluator.toMatchRepresentation(matchRepresentation) + " ) "
                + match.toString();
    }
}
