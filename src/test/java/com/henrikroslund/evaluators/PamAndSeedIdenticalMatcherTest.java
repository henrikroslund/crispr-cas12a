package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class PamAndSeedIdenticalMatcherTest {

    @Test
    public void testEvaluateEqual() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertTrue(new IdenticalEvaluator(sequence).evaluate(sequence));
    }

    @Test
    public void testEvaluateNotEqual() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        Sequence sequence2 = new Sequence("TATAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertFalse(new IdenticalEvaluator(sequence).evaluate(sequence2));
    }
}