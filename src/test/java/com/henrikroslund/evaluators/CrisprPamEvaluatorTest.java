package com.henrikroslund.evaluators;

import com.henrikroslund.TestUtils;
import com.henrikroslund.sequence.Sequence;
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
    }
}