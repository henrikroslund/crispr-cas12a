package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

public class GCContentN1N20Evaluator implements SequenceEvaluator {

    private final Range<Integer> range;

    @Getter
    private Sequence match = null;

    public GCContentN1N20Evaluator() {
        range = Range.between(9,11);
    }

    public GCContentN1N20Evaluator(Range<Integer> range) {
        this.range = range;
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int gcCount = sequence.getGCCount();
        if(range.contains(gcCount)) {
            match = sequence;
            return true;
        } else {
            match = null;
            return false;
        }
    }

    @Override
    public String describe() {
        return "GCContentN1N20Evaluator(" + range + ")";
    }

    @Override
    public String toString() {
        return "GCContentN1N20Evaluator " + match.toString();
    }

}
