package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

import java.util.List;

public interface SequenceEvaluator {
    boolean evaluate(Sequence sequence);

    /**
     * Will return true if ALL the evaluators returns true
     */
    static boolean matchAll(List<SequenceEvaluator> evaluators, Sequence sequence) {
        for(SequenceEvaluator evaluator : evaluators) {
            if(!evaluator.evaluate(sequence)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Will return true if ANY the evaluators returns true
     */
    static boolean matchAny(List<SequenceEvaluator> evaluators, Sequence sequence) {
        for(SequenceEvaluator evaluator : evaluators) {
            if(evaluator.evaluate(sequence)) {
                return true;
            }
        }
        return false;
    }
}
