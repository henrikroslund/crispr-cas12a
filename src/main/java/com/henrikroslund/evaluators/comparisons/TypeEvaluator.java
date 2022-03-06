package com.henrikroslund.evaluators.comparisons;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeEvaluator implements SequenceEvaluator {

    public enum Type {
        TYPE_1, // >=X mismatches in PAM's first 3
        TYPE_2, // >=X consecutive mismatches in Seed
        TYPE_3, // If Type_1 & Type_2
        TYPE_4, // Did not bind in genome. Cannot be evaluated in this class on a sequence comparison level.
        TYPE_5, // >=X mismatches in N7-N20
        TYPE_6, // >=X mismatches in Seed
        TYPE_DISCARD // If no other type applies
    }

    private final int type1Criteria;
    private final int type2Criteria;
    private final int type5Criteria;
    private final int type6Criteria;

    final Sequence sequence;

    @Getter
    private Sequence match = null;
    @Getter
    private final List<Type> matchTypes = new ArrayList<>();
    private final String[] matchRepresentation = new String[]{"?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?","?"};

    public TypeEvaluator(Sequence sequence, int type1Criteria, int type2Criteria, int type5Criteria, int type6Criteria) {
        this.sequence = sequence;
        this.type1Criteria = type1Criteria;
        this.type2Criteria = type2Criteria;
        this.type5Criteria = type5Criteria;
        this.type6Criteria = type6Criteria;
    }

    public TypeEvaluator(Sequence sequence) {
        this(sequence, 2, 2, 3, 3);
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
        return true;
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new TypeEvaluator(sequence, type1Criteria, type2Criteria, type5Criteria, type6Criteria);
    }

    private void evaluateTypes(int pamMismatches, int seedMismatches, int seedMismatchesInARow, int mismatchesN7toN20) {
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
        if(seedMismatches >= type6Criteria) {
            matchTypes.add(Type.TYPE_6);
        }
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
                + Type.TYPE_6.name() + ": " + type6Criteria + " "
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
