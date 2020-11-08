package com.henrikroslund.evaluators;

import com.henrikroslund.Sequence;

public class IdenticalEvaluator implements SequenceEvaluator {

    Sequence sequence;

    public IdenticalEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        return this.sequence.equals(sequence);
    }
}
