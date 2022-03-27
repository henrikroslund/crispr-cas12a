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
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoConsecutiveIdenticalN1N20Evaluator implements SequenceEvaluator {

    public enum Type {
        TRIPLE(3), QUADRUPLE(4);
        protected final int value;
        Type(int i) {
            this.value = i;
        }
    }

    // Match 4 characters and then group the 5 character and then see if that character repeats two more times
    private final Pattern TARGET_MATCH_TRIPLETS_CONTENT_PATTERN;

    @Getter
    private Sequence match = null;

    private final Type type;

    public NoConsecutiveIdenticalN1N20Evaluator(Type type) {
        this.type = type;
        TARGET_MATCH_TRIPLETS_CONTENT_PATTERN = Pattern.compile("(.)\\1{"+(type.value-1)+"}");
    }

    /**
     * Returns true if same character does not repeat consecutively according to configuration in the N1 to N20, otherwise false
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
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new NoConsecutiveIdenticalN1N20Evaluator(type);
    }

    @Override
    public String describe() {
        return "NoConsecutiveIdenticalN1N20Evaluator( " + type + " )";
    }

    @Override
    public String toString() {
        return describe() + " " + match.toString();
    }
}
