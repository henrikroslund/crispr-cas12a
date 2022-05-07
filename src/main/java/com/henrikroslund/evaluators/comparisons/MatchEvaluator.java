package com.henrikroslund.evaluators.comparisons;

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

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Range;

import java.util.Collections;
import java.util.List;

@Log
public class MatchEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    private final Range<Integer> matchRange;
    private final List<Range<Integer>> rangeIndexesToCompare;
    private final boolean[] indexesToCompare = new boolean[Sequence.RAW_LENGTH];

    @Getter
    private Sequence match = null;
    private int matches = -1;

    public MatchEvaluator(Sequence sequence, Range<Integer> matchRange, List<Range<Integer>> rangeIndexesToCompare) {
        this.matchRange = matchRange;
        this.sequence = sequence;
        this.rangeIndexesToCompare = rangeIndexesToCompare;
        for (Range<Integer> range : rangeIndexesToCompare) {
            for (int i = 0; i < indexesToCompare.length; i++) {
                if (range.contains(i)) {
                    indexesToCompare[i] = true;
                }
            }
        }
    }

    public MatchEvaluator(Sequence sequence, Range<Integer> matchRange) {
        this(sequence, matchRange, Collections.singletonList(Range.between(0,24)));
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMatches = 0;

        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            if(indexesToCompare[i]) {
                if(this.sequence.getRaw().charAt(i) == sequence.getRaw().charAt(i)) {
                    numberOfMatches++;
                }
            }
        }

        if(matchRange.contains(numberOfMatches)) {
            this.match = sequence;
            this.matches = numberOfMatches;
            handleEvaluationMatch(log);
            return true;
        } else {
            this.matches = -1;
            this.match = null;
            return false;
        }
    }

    @Override
    public SequenceEvaluator getNewEvaluator(Sequence sequence) {
        return new MatchEvaluator(sequence, matchRange, rangeIndexesToCompare);
    }

    @Override
    public String describe() {
        return "MatchEvaluator(matches: " + matchRange + " indexes: " + rangeIndexesToCompare + " )";
    }

    @Override
    public String toString() {
        return describe() + " matches(" + matches + ") " + match.toString();
    }
}
