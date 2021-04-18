package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class CrisprPamEvaluator implements SequenceEvaluator {

    // First three must be Ts and the forth must Not be T for crispr

    // Strict mapping require TTT^T while NOT strict will only check TTTN
    private boolean strictMatching;

    public CrisprPamEvaluator(boolean strictMatching) {
        this.strictMatching = strictMatching;
    }

    @Getter
    private Sequence match = null;

    @Override
    public boolean evaluate(Sequence sequence) {
        match = null;
        boolean result =
                sequence.getRaw().charAt(0) == 'T' &&
                sequence.getRaw().charAt(1) == 'T' &&
                sequence.getRaw().charAt(2) == 'T' &&
                ( !strictMatching || sequence.getRaw().charAt(3) != 'T');
        if(result) {
            match = sequence;
        }
        return result;
    }

    @Override
    public String describe() {
        return "CrisprPamEvaluator( " + (strictMatching ? "TTT^T" : "TTTN") + " )";
    }

    @Override
    public String toString() {
        return describe() + " " + match;
    }
}
