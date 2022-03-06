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

import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

        // 24 mismatches
        for(int i=0; i<22; i++) {
            MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.is(24));
            StringBuilder builder = new StringBuilder("AAAAAAAAAAAAAAAAAAAAAAAT");

            assertFalse(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));

            builder.setCharAt(23, 'A');
            assertTrue(evaluator.evaluate(new Sequence(builder.toString(), 0, "")));
        }
    }

    @Test
    public void testMismatchedRangedIndexes() {
        String original = "TTTTTTTTTTTTTTTTTTTTTTTT";
        Sequence sequence = new Sequence(original, 0, "");
        MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.is(0));

        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("TAAAAAAAAAAAAAAAAAAAAAAA", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("ATTTTTTTTTTTTTTTTTTTTTTT", 0, "")));

        evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.is(1));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("ATAAAAAAAAAAAAAAAAAAAAAA", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("TATTTTTTTTTTTTTTTTTTTTTT", 0, "")));

        evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.is(23));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("AAAAAAAAAAAAAAAAAAAAAAAT", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTA", 0, "")));

        evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.between(10,11));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("AAAAAAAAAATTAAAAAAAAAAAA", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTATTTTTTTTTTTTT", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTTATTTTTTTTTTTT", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTAATTTTTTTTTTTT", 0, "")));

        evaluator = new MismatchEvaluator(sequence, Range.between(1,2), Range.between(10,11));
        assertFalse(evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "")));
        assertFalse(evaluator.evaluate(new Sequence("AAAAAAAAAATTAAAAAAAAAAAA", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTATTTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTTATTTTTTTTTTTT", 0, "")));
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTTTTTAATTTTTTTTTTTT", 0, "")));
    }

    @Test
    public void testToString() {
        String original = "TTTTTTTTTTTTTTTTTTTTTTTT";
        Sequence sequence = new Sequence(original, 0, "");
        MismatchEvaluator evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.is(0));

        assertTrue(evaluator.toString().contains("???? ????? ????? ????? ?????"));

        evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, ""));
        assertTrue(evaluator.toString().contains("=??? ????? ????? ????? ?????"));

        evaluator = new MismatchEvaluator(sequence, Range.is(0), Range.between(0,23));
        evaluator.evaluate(new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, ""));
        assertTrue(evaluator.toString().contains("==== ===== ===== ===== ====="));
        evaluator.evaluate(new Sequence("AAAAAAAAAAAAAAAAAAAAAAAA", 0, ""));
        assertTrue(evaluator.toString().contains("XXXX XXXXX XXXXX XXXXX XXXXX"));
    }
}
