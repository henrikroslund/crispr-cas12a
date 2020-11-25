package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static org.junit.Assert.*;

public class NoTripletN1N20EvaluatorTest {

    @Test
    public void testIgnorePamMatchNoTriplets() {
        NoTripletN1N20Evaluator evaluator = new NoTripletN1N20Evaluator();
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTGCGCGCGCGCGCGCGCGC",0,"test")));
    }

    @Test
    public void testHasTriplets() {
        NoTripletN1N20Evaluator evaluator = new NoTripletN1N20Evaluator();
        assertFalse(evaluator.evaluate(new Sequence("TTTCACACAGGCACCCCGTCATTA",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCTTTCGCGCGCGCGCGCGCGC",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCGCGCGCGCGCGCGCGCGTTT",0,"test")));
    }
}