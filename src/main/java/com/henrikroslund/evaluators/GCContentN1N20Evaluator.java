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
import org.apache.commons.lang3.Range;

public class GCContentN1N20Evaluator implements SequenceEvaluator {

    private final Range<Integer> range;

    @Getter
    private Sequence match = null;

    public GCContentN1N20Evaluator() {
        this(Range.between(9,11));
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
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new GCContentN1N20Evaluator(range);
    }

    @Override
    public String describe() {
        return "GCContentN1N20Evaluator(" + range + ")";
    }

    @Override
    public String toString() {
        return describe() + " " +  match.toString();
    }

}
