package com.henrikroslund.evaluators;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.sequence.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public interface SequenceEvaluator {

    String MATCH_CHAR = "=";
    String MISMATCH_CHAR = "X";

    boolean evaluate(Sequence sequence);

    Sequence getMatch();

    SequenceEvaluator getNewEvaluator(Sequence sequence);

    default void handleEvaluationMatch(Logger log) {
        if(EvaluatorConfig.logEvaluationMatch) {
            log.info(toString());
        }
    }

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

    static List<SequenceEvaluator> getNewEvaluators(Sequence sequence, List<SequenceEvaluator> evaluators) {
        List<SequenceEvaluator> result = new ArrayList<>();
        for(SequenceEvaluator evaluator : evaluators) {
            result.add(evaluator.getNewEvaluator(sequence));
        }
        return result;
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
