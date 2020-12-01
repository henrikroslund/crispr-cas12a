package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

public class GCContentN1N20Evaluator implements SequenceEvaluator {

    private final int LOW_LIMIT;
    private final int HIGH_LIMIT;

    @Getter
    private Sequence match = null;

    public GCContentN1N20Evaluator() {
        LOW_LIMIT = 9;
        HIGH_LIMIT = 11;
    }

    public GCContentN1N20Evaluator(int minCount, int maxCount) {
        LOW_LIMIT = minCount;
        HIGH_LIMIT = maxCount;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int gcCount = sequence.getGCCount();
        boolean result = gcCount >= LOW_LIMIT && gcCount <= HIGH_LIMIT;
        if(result) {
            match = sequence;
        } else {
            match = null;
        }
        return result;
    }

    @Override
    public String toString() {
        return "GCContentN1N20Evaluator " + match.toString();
    }

}
