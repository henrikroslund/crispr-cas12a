package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.util.Collections;
import java.util.List;

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
