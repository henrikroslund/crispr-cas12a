package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

public class CrisprPamEvaluator implements SequenceEvaluator {

    // First three must be Ts and the forth must Not be T for crispr
    private static final String CRISPR_PAM_MATCH_REGEXP = "^[T]{3}[^T].*";

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = sequence.getRaw().matches(CRISPR_PAM_MATCH_REGEXP);
        return result;
    }

}
