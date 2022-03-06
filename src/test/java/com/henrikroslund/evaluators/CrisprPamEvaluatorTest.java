package com.henrikroslund.evaluators;

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
