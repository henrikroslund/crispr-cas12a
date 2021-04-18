package com.henrikroslund.evaluators.comparisons;

import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import static org.junit.Assert.*;

public class MismatchEvaluatorTest {

    @Test
    public void testMismatchCompareAllIndexes() {
        String original = "TTTTTTTTTTTTTTTTTTTTTTTT";
        Sequence sequence = new Sequence(original, 0, "");

        // Full match
        assertTrue(new MismatchEvaluator(sequence, Range.is(0)).evaluate(sequence));

        // No mismatches allowed
        for(int i=0; i<24; i++) {
            MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.is(0));
            StringBuilder builder = new StringBuilder(original);
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
            builder.setCharAt(i, 'A');
            assertFalse(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
        }

        // 0 - 1 mismatches
        for(int i=0; i<23; i++) {
            MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.between(0, 1));
            StringBuilder builder = new StringBuilder(original);
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
            builder.setCharAt(i, 'A');
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
            builder.setCharAt(i+1, 'A');
            assertFalse(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
        }

        // 1 - 2 mismatches
        for(int i=0; i<22; i++) {
            MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.between(1, 2));
            StringBuilder builder = new StringBuilder(original);

            assertFalse(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));

            builder.setCharAt(i, 'A');
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));

            builder.setCharAt(i+1, 'A');
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));

            builder.setCharAt(i+2, 'A');
            assertFalse(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
        }

    }

}