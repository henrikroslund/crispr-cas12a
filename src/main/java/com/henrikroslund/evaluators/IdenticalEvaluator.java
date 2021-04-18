package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class IdenticalEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private Sequence match = null;

    private boolean checkPam = true;
    private boolean checkSeed = true;
    private boolean checkN7N20 = true;
    private boolean checkAll = true;

    public IdenticalEvaluator(Sequence sequence) {
        this.sequence = sequence;
    }

    public IdenticalEvaluator(Sequence sequence, boolean checkPam, boolean checkSeed, boolean checkN7N20) {
        this.sequence = sequence;
        this.checkPam = checkPam;
        this.checkSeed = checkSeed;
        this.checkN7N20 = checkN7N20;
        this.checkAll = checkPam && checkSeed && checkN7N20;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        boolean result = true;
        if(checkAll) {
            result = this.sequence.equals(sequence);
        } else {
            if(checkPam) {
                result = this.sequence.equalsPam(sequence);
            }
            if(checkSeed) {
                result = result && this.sequence.equalsSeed(sequence);
            }
            if(checkN7N20) {
                result = result && this.sequence.equalsSeed(sequence);
            }
        }
        if(result) {
            match = sequence;
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new IdenticalEvaluator(sequence, checkPam, checkSeed, checkN7N20);
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
