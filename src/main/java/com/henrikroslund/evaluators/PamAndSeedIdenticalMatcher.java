package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.Setter;

public class PamAndSeedIdenticalMatcher implements SequenceEvaluator {

    private final Sequence sequence;

    @Getter
    @Setter
    private Sequence match;


    public PamAndSeedIdenticalMatcher(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        return this.sequence.equalsPam(sequence) && this.sequence.equalsSeed(sequence);
    }

    @Override
    public String toString() {
        return "PamAndSeedIdenticalMatcher " + match.toString();
    }

}
