package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

public class NoTripletN1N20Evaluator implements SequenceEvaluator {

    // Match 4 characters and then group the 5 character and then see if that character repeats two more times
    private static final Pattern TARGET_MATCH_TRIPLETS_CONTENT_PATTERN = Pattern.compile("(?:....)(.)\\1{2}");

    @Getter
    @Setter
    private Sequence match;

    /**
     * Returns true of there are no tripples in the N1 to N20, otherwise false
     */
    @Override
    public boolean evaluate(Sequence sequence) {
        return !TARGET_MATCH_TRIPLETS_CONTENT_PATTERN.matcher(sequence.getRaw()).lookingAt();
    }

    @Override
    public String toString() {
        return "NoTripletN1N20Evaluator " + match.toString();
    }
}
