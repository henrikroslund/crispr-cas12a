package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoTripletN1N20Evaluator implements SequenceEvaluator {

    // Match 4 characters and then group the 5 character and then see if that character repeats two more times
    private static final Pattern TARGET_MATCH_TRIPLETS_CONTENT_PATTERN = Pattern.compile("(.)\\1{2}");

    @Getter
    private Sequence match = null;

    @Override
    public SequenceEvaluator clone() {
        return new NoTripletN1N20Evaluator();
    }

    /**
     * Returns true of there are no tripples in the N1 to N20, otherwise false
     */
    @Override
    public boolean evaluate(Sequence sequence) {
        Matcher matcher = TARGET_MATCH_TRIPLETS_CONTENT_PATTERN.matcher(sequence.getRaw()).region(Sequence.SEED_INDEX_START, Sequence.RAW_LENGTH);
        boolean result = matcher.find();
        if(!result) {
            match = sequence;
        } else {
            match = null;
        }
        return !result;
    }

    @Override
    public String describe() {
        return "NoTripletN1N20Evaluator";
    }

    @Override
    public String toString() {
        return describe() + " " + match.toString();
    }
}
