package com.henrikroslund.evaluators;

import com.henrikroslund.TestUtils;
import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class ComparisonEvaluatorTest {

    @Test
    public void testFullMatch() {
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "Test");
        ComparisonEvaluator evaluator = new ComparisonEvaluator(sequence, 24, 24);

        assertTrue(evaluator.evaluate(sequence));
        assertSame(sequence, evaluator.getMatch());
        assertEquals(24, evaluator.getMatches());

        assertFalse(evaluator.evaluate(new Sequence("ATTTTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
        assertNull(evaluator.getMatch());
        assertEquals(-1, evaluator.getMatches());
    }

    @Test
    public void testFullMismatch() {
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "Test");
        ComparisonEvaluator evaluator = new ComparisonEvaluator(sequence, 0, 0);
        assertTrue(evaluator.evaluate(new Sequence("AAAAAAAAAAAAAAAAAAAAAAAA", 0, "Test")));
        assertNotSame(sequence, evaluator.getMatch());
        assertEquals(0, evaluator.getMatches());
    }

    @Test
    public void testBoundary() {
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "Test");
        ComparisonEvaluator evaluator = new ComparisonEvaluator(sequence, 22, 23);
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
        assertTrue(evaluator.evaluate(new Sequence("ATTTTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
        assertTrue(evaluator.evaluate(new Sequence("AATTTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
        assertFalse(evaluator.evaluate(new Sequence("AAATTTTTTTTTTTTTTTTTTTTT", 0, "Test")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLimits() {
        new ComparisonEvaluator(new Sequence(TestUtils.VALID_CRISPR_SEQUENCE, 0, "Test"), 2, 1);
    }

}