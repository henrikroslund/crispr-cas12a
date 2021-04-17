package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.apache.commons.lang3.StringUtils;
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
        String raw1 = "TTTAGCGCGCGCGTTTTTTTTTTT";
        String raw2 = "TTTAGCGCGCGCGTTTTTTTTTTT";
        assertTrue(new IdenticalEvaluator(new Sequence(raw1,0,"test")).evaluate(new Sequence(raw2, 0, "test")));
    }

    @Test
    public void testNotSame() {
        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTA", 5, "test");
        Sequence sequence2 = new Sequence("ATTAGCGCGCGCGTTTTTTTTTTT", 5, "test");
        assertFalse(new IdenticalEvaluator(sequence).evaluate(sequence2));
    }

    @Test
    public void testToString() {
        assertFalse(StringUtils.isEmpty(new IdenticalEvaluator(null).toString()));

        Sequence sequence = new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT",0,"test");
        IdenticalEvaluator evaluator = new IdenticalEvaluator(sequence);
        assertTrue(evaluator.evaluate(sequence));
        assertEquals(sequence, evaluator.getMatch());
        assertTrue(evaluator.toString().contains(sequence.getRaw()));
    }

    @Test
    public void testDescribe() {
        assertFalse(StringUtils.isEmpty(new IdenticalEvaluator(null).describe()));
    }
}