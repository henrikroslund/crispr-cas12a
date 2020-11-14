package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdenticalEvaluatorTest {

    @Test
    public void testSameObject() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertTrue(new IdenticalEvaluator(sequence).evaluate(sequence));
    }

    @Test
    public void testSameNewString() {
        String raw = new String("TTTAGCGCGCGCGTTTTTTTTTTT");
        String raw2 = new String("TTTAGCGCGCGCGTTTTTTTTTTT");
        assertTrue(new IdenticalEvaluator(new Sequence(raw,0,"test")).evaluate(new Sequence(raw2, 0, "test")));
    }

    @Test
    public void testNotSame() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTA", 5, "test");
        Sequence sequence2 = new Sequence("ATTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertFalse(new IdenticalEvaluator(sequence).evaluate(sequence2));
    }

}