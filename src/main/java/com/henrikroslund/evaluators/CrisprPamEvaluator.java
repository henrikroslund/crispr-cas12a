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
        return new CrisprPamEvaluator();
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = sequence.getRaw().matches(CRISPR_PAM_MATCH_REGEXP);
        if(result) {
            match = sequence;
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public String describe() {
        return "CrisprPamEvaluator( " + CRISPR_PAM_MATCH_REGEXP + " )";
    }

    @Override
    public String toString() {
        return describe() + " " + match.toString();
    }
}
