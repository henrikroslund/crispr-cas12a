package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class CrisprPamEvaluator implements SequenceEvaluator {

    // First three must be Ts and the forth must Not be T for crispr
    private static final String CRISPR_PAM_MATCH_REGEXP = "^[T]{3}[^T].*";

    @Getter
    private Sequence match = null;

    @Override
    public SequenceEvaluator clone() {
        CrisprPamEvaluator clone = new CrisprPamEvaluator();
        clone.match = getMatch();
        return clone;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        match = null;
        for(int i=0; i<=2; i++) {
            if(sequence.getRaw().charAt(i) != 'T') {
                return false;
            }
        }
        if(sequence.getRaw().charAt(3) == 'T') {
            return false;
        }
        match = sequence;
        return true;
    }

    @Override
    public String describe() {
        return "CrisprPamEvaluator( " + CRISPR_PAM_MATCH_REGEXP + " )";
    }

    @Override
    public String toString() {
        return describe() + " " + match;
    }
}
