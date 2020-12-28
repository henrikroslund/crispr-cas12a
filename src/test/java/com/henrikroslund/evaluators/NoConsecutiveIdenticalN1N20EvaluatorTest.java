package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.QUADRUPLE;
import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.TRIPLE;
import static org.junit.Assert.*;

public class NoConsecutiveIdenticalN1N20EvaluatorTest {

    @Test
    public void testIgnorePamMatchNoTriplets() {
        NoConsecutiveIdenticalN1N20Evaluator evaluator = new NoConsecutiveIdenticalN1N20Evaluator(TRIPLE);
        assertTrue(evaluator.evaluate(new Sequence("TTTTTTGCGCGCGCGCGCGCGCGC",0,"test")));
    }

    @Test
    public void testIgnorePamMatchNoQuadruples() {
        NoConsecutiveIdenticalN1N20Evaluator evaluator = new NoConsecutiveIdenticalN1N20Evaluator(QUADRUPLE);
        assertTrue(evaluator.evaluate(new Sequence("TTTCACACAGGCACCCGGTCATTA",0,"test")));
    }

    @Test
    public void testHasTriplets() {
        NoConsecutiveIdenticalN1N20Evaluator evaluator = new NoConsecutiveIdenticalN1N20Evaluator(TRIPLE);
        assertFalse(evaluator.evaluate(new Sequence("TTTCACACAGGCACCCCGTCATTA",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCTTTCGCGCGCGCGCGCGCGC",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCGCGCGCGCGCGCGCGCGTTT",0,"test")));
    }

    @Test
    public void testHasQuadruples() {
        NoConsecutiveIdenticalN1N20Evaluator evaluator = new NoConsecutiveIdenticalN1N20Evaluator(QUADRUPLE);
        assertFalse(evaluator.evaluate(new Sequence("TTTCACACAGGCACCCCGTCATTA",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCTTTTGCGCGCGCGCGCGCGC",0,"test")));
        assertFalse(evaluator.evaluate(new Sequence("GCGCGCGCGCGCGCGCGCGCTTTT",0,"test")));
    }
}