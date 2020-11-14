package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.Setter;

public class GCContentN1N20Evaluator implements SequenceEvaluator {

    private static final int LOW_LIMIT = 9; //20*0.45
    private static final int HIGH_LIMIT = 11; //20*0.55

    @Getter
    @Setter
    private Sequence match;

    @Override
    public boolean evaluate(Sequence sequence) {
        int count = 0;
        for(int i=Sequence.PAM_LENGTH; i<sequence.getRaw().length(); i++) {
            char currentChar = sequence.getRaw().charAt(i);
            if(currentChar == 'G' || currentChar == 'C') {
                count++;
            }
        }
        return count >= LOW_LIMIT && count <= HIGH_LIMIT;
    }

    @Override
    public String toString() {
        return "GCContentN1N20Evaluator " + match.toString();
    }

}
