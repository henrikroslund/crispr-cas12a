package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.Setter;

public class IdenticalEvaluator implements SequenceEvaluator {

    Sequence sequence;

    @Getter
    @Setter
    private Sequence match;


    public IdenticalEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        return this.sequence.equals(sequence);
    }

    @Override
    public String toString() {
        return "IdenticalEvaluator " + match.toString();
    }
}
