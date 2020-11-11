package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

import java.util.List;

public interface SequenceEvaluator {
    boolean evaluate(Sequence sequence);

    /**
     * Will return true of all the evaluators returns true
     * @param evaluators
     * @return
     */
    static boolean matchAll(List<SequenceEvaluator> evaluators, Sequence sequence) {
        for(SequenceEvaluator evaluator : evaluators) {
            if(!evaluator.evaluate(sequence)) {
                return false;
            }
        }
        return true;
    };
}
