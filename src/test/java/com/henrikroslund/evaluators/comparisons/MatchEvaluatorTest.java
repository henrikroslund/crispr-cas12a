package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import static org.junit.Assert.*;

public class MatchEvaluatorTest {

    @Test
    public void testFullMatch() {
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "Test");
        MatchEvaluator evaluator = new MatchEvaluator(sequence, Range.between(24, 24));

        assertTrue(evaluator.evaluate(sequence));
        assertSame(sequence, evaluator.getMatch());

        assertFalse(evaluator.evaluate(new Sequence("ATTTTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
        assertNull(evaluator.getMatch());
    }
}