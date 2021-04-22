package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class PamAndSeedIdenticalMatcherTest {

    @Test
    public void testEvaluateEqual() {
        Sequence sequence = new Sequence("TTTAGGGGGGTTTTTTTTTTTTTT", 5, "test");
        assertTrue(new PamAndSeedIdenticalMatcher(sequence).evaluate(sequence));
        assertTrue(new PamAndSeedIdenticalMatcher(sequence).evaluate(new Sequence("TTTAGGGGGGATTTTTTTTTTTTT", 5, "test")));
    }

    @Test
    public void testEvaluateNotEqual() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertFalse(new PamAndSeedIdenticalMatcher(sequence).evaluate(new Sequence("TATAGCGCGCGCGTTTTTTTTTTT", 5, "test")));
        assertFalse(new PamAndSeedIdenticalMatcher(sequence).evaluate(new Sequence("TTTAGCGCGTTTTTTTTTTTTTTT", 5, "test")));
    }

    @Test
    public void testGetNewEvaluator() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        PamAndSeedIdenticalMatcher matcher = new PamAndSeedIdenticalMatcher(sequence);
        assertEquals(sequence, matcher.getSequence());

        Sequence sequence2 = new Sequence("TTTAGCGCGCGCGTTTTAAAAAAA", 5, "test");
        assertTrue(matcher.evaluate(sequence2));
        assertEquals(sequence2, matcher.getMatch());
        assertNull(matcher.getNewEvaluator(sequence).getMatch());
    }
}