package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class IdenticalEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private Sequence match = null;

    public IdenticalEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = this.sequence.equals(sequence);
        if(result) {
            match = sequence;
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public String describe() {
        return "IdenticalEvaluator";
    }

    @Override
    public String toString() {
        return describe() + " " + match;
    }
}
