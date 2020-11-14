package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

public class PamAndSeedIdenticalMatcher implements SequenceEvaluator {

    private final Sequence sequence;

    public PamAndSeedIdenticalMatcher(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        return this.sequence.equalsPam(sequence) && this.sequence.equalsSeed(sequence);
    }
}
