package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class PamAndSeedIdenticalMatcher implements SequenceEvaluator {

    private final Sequence sequence;

    @Getter
    private Sequence match = null;


    public PamAndSeedIdenticalMatcher(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = this.sequence.equalsPam(sequence) && this.sequence.equalsSeed(sequence);
        if(result) {
            match = sequence;
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return "PamAndSeedIdenticalMatcher " + match.toString();
    }

}