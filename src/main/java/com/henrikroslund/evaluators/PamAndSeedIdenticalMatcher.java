package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

public class PamAndSeedIdenticalMatcher implements SequenceEvaluator {

    private static int PAM_AND_SEED_LENGTH = Sequence.PAM_LENGTH + Sequence.SEED_LENGTH;
    private Sequence sequence;

    public PamAndSeedIdenticalMatcher(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        return this.sequence.getRaw().regionMatches(0, sequence.getRaw(), 0, PAM_AND_SEED_LENGTH);
    }
}
