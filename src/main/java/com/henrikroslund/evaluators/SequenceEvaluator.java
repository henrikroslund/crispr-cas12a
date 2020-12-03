package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

import java.util.List;

public interface SequenceEvaluator {

    SequenceEvaluator clone();

    boolean evaluate(Sequence sequence);

    Sequence getMatch();

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
    static SequenceEvaluator matchAny(List<SequenceEvaluator> evaluators, Sequence sequence) {
        for(SequenceEvaluator evaluator : evaluators) {
            if(evaluator.evaluate(sequence)) {
                return evaluator;
            }
        }
        return null;
    }

    String describe();

}
