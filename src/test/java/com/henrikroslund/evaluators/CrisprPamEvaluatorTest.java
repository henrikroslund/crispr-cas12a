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

import com.henrikroslund.TestUtils;
import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CrisprPamEvaluatorTest {

    @Test
    public void evaluateValidStrict() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(true);
        assertTrue(evaluator.evaluate(new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "test")));
    }

    @Test
    public void evaluateValidLoose() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(false);
        assertTrue(evaluator.evaluate(new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "test")));
        assertTrue(evaluator.evaluate(new Sequence(TestUtils.VALID_LOOSE_CRISPR_SEQUENCE, 0, "test")));
    }

    @Test
    public void evaluateValidMatchAll() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(true);
        Sequence validSequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "test");
        assertTrue(evaluator.evaluate(validSequence));
        assertTrue(SequenceEvaluator.matchAll(Collections.singletonList(new CrisprPamEvaluator(true)), validSequence));
    }

    @Test
    public void evaluateInvalidStrict() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(true);
        assertFalse(evaluator.evaluate(new Sequence(TestUtils.INVALID_STRICT_CRISPR_SEQUENCE, 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TTAACCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TATTCCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("ATTTCCCCCAAAAACCCCCAAAAG", 0, "test")));
    }

    @Test
    public void evaluateInvalidLoose() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(false);
        assertFalse(evaluator.evaluate(new Sequence(TestUtils.INVALID_LOOSE_CRISPR_SEQUENCE, 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TTAACCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TATTCCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("ATTTCCCCCAAAAACCCCCAAAAG", 0, "test")));
    }


    @Test
    public void testDescribe() {
        assertFalse(StringUtils.isEmpty(new CrisprPamEvaluator(true).describe()));
    }

    @Test
    public void testMatch() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator(true);
        assertNull(evaluator.getMatch());
        assertFalse(StringUtils.isEmpty(evaluator.toString()));
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "test");
        assertTrue(evaluator.evaluate(sequence));
        assertTrue(evaluator.toString().contains(TestUtils.VALID_STRICT_CRISPR_SEQUENCE));
        assertEquals(sequence, evaluator.getMatch());
    }
}
