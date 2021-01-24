package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoConsecutiveIdenticalN1N20Evaluator implements SequenceEvaluator {

    public static final int TRIPLE = 3;
    public static final int QUADRUPLE = 4;

    // Match 4 characters and then group the 5 character and then see if that character repeats two more times
    private final Pattern TARGET_MATCH_TRIPLETS_CONTENT_PATTERN;

    @Getter
    private Sequence match = null;

    private final int number;

    public NoConsecutiveIdenticalN1N20Evaluator(int number) {
        this.number = number;
        TARGET_MATCH_TRIPLETS_CONTENT_PATTERN = Pattern.compile("(.)\\1{"+(number-1)+"}");
    }

    @Override
    public SequenceEvaluator clone() {
        return new NoConsecutiveIdenticalN1N20Evaluator(this.number);
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