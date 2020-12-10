package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.evaluators.SequenceEvaluator;
import com.henrikroslund.sequence.Sequence;
import lombok.Getter;
import org.apache.commons.lang3.Range;

import java.util.Collections;
import java.util.List;

public class MatchEvaluator implements SequenceEvaluator {

    final Sequence sequence;

    @Getter
    private final Range<Integer> matchRange;
    @Getter
    private final List<Range<Integer>> indexesToCompare;

    @Getter
    private Sequence match = null;
    @Getter
    private int matches = -1;

    public MatchEvaluator(Sequence sequence, Range<Integer> matchRange, List<Range<Integer>> indexesToCompare) {
        this.matchRange = matchRange;
        this.sequence = sequence;
        this.indexesToCompare = indexesToCompare;
    }

    public MatchEvaluator(Sequence sequence, Range<Integer> matchRange) {
        this(sequence, matchRange, Collections.singletonList(Range.between(0,24)));
    }

    @Override
    public SequenceEvaluator clone() {
        return new MatchEvaluator(this.sequence, this.matchRange);
    }

    @Override
    public boolean evaluate(Sequence sequence) {
        int numberOfMatches = 0;

        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            if(this.sequence.getRaw().charAt(i) == sequence.getRaw().charAt(i)) {
                numberOfMatches++;
            }
        }

        // TODO need to write tests for the indexesToCompare...
        for(int i=0; i<Sequence.RAW_LENGTH; i++) {
            int finalI = i;
            boolean inRange = indexesToCompare.stream().anyMatch(integerRange -> integerRange.contains(finalI));
            if(inRange) {
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
    public String describe() {
        return "MatchEvaluator(" + matchRange + ")";
    }

    @Override
    public String toString() {
        return describe() + " matches(" + matches + ") " + match.toString();
    }
}
