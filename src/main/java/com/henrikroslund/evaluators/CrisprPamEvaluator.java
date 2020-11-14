package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.Setter;

public class CrisprPamEvaluator implements SequenceEvaluator {

    // First three must be Ts and the forth must Not be T for crispr
    private static final String CRISPR_PAM_MATCH_REGEXP = "^[T]{3}[^T].*";

    @Getter
    @Setter
    private Sequence match;

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = sequence.getRaw().matches(CRISPR_PAM_MATCH_REGEXP);
        return result;
    }

    @Override
    public String toString() {
        return "CrisprPamEvaluator " + match.toString();
    }
}
