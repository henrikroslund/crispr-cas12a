package com.henrikroslund.evaluators;

import com.henrikroslund.TestUtils;
import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CrisprPamEvaluatorTest {

    @Test
    public void evaluateValid() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator();
        assertTrue(evaluator.evaluate(new Sequence(TestUtils.VALID_CRISPR_SEQUENCE, 0, "test")));
    }

    @Test
    public void evaluateValidMatchAll() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator();
        Sequence validSequence = new Sequence(TestUtils.VALID_CRISPR_SEQUENCE, 0, "test");
        assertTrue(evaluator.evaluate(validSequence));
        assertTrue(SequenceEvaluator.matchAll(Collections.singletonList(new CrisprPamEvaluator()), validSequence));
    }

    @Test
    public void evaluateInvalid() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator();
        assertFalse(evaluator.evaluate(new Sequence(TestUtils.INVALID_CRISPR_SEQUENCE, 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TTAACCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("TATTCCCCCAAAAACCCCCAAAAG", 0, "test")));
        assertFalse(evaluator.evaluate(new Sequence("ATTTCCCCCAAAAACCCCCAAAAG", 0, "test")));
    }

    @Test
    public void testDescribe() {
        assertFalse(StringUtils.isEmpty(new CrisprPamEvaluator().describe()));
    }

    @Test
    public void testMatch() {
        CrisprPamEvaluator evaluator = new CrisprPamEvaluator();
        assertNull(evaluator.getMatch());
        assertFalse(StringUtils.isEmpty(evaluator.toString()));
        Sequence sequence = new Sequence(TestUtils.VALID_CRISPR_SEQUENCE, 0, "test");
        assertTrue(evaluator.evaluate(sequence));
        assertTrue(evaluator.toString().contains(TestUtils.VALID_CRISPR_SEQUENCE));
        assertEquals(sequence, evaluator.getMatch());
    }
}