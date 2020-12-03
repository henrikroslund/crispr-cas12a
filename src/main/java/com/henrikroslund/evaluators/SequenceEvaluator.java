package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;

import java.util.List;

public interface SequenceEvaluator {

    String MATCH_CHAR = "=";
    String MISMATCH_CHAR = "X";

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

    static String toMatchRepresentation(String[] matchRepresentation) {
        return matchRepresentation[0]
                + matchRepresentation[1]
                + matchRepresentation[2]
                + matchRepresentation[3] + " "
                + matchRepresentation[4]
                + matchRepresentation[5]
                + matchRepresentation[6]
                + matchRepresentation[7]
                + matchRepresentation[8] + " "
                + matchRepresentation[9]
                + matchRepresentation[10]
                + matchRepresentation[11]
                + matchRepresentation[12]
                + matchRepresentation[13] + " "
                + matchRepresentation[14]
                + matchRepresentation[15]
                + matchRepresentation[16]
                + matchRepresentation[17]
                + matchRepresentation[18] + " "
                + matchRepresentation[19]
                + matchRepresentation[20]
                + matchRepresentation[21]
                + matchRepresentation[22]
                + matchRepresentation[23];
    }

}
