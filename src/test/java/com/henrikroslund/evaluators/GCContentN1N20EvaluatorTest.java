package com.henrikroslund.evaluators;

import com.henrikroslund.sequence.Sequence;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GCContentN1N20EvaluatorTest {

    @Test
    public void testLowLimitGC() {
        assertTrue(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTAGCGCGCGCGTTTTTTTTTTT", 5, "test")));
        assertTrue(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTATTTTTTTTTTTGCGCGCGCG", 5, "test")));

        assertFalse(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTATCGCGCGCGTTTTTTTTTTT", 5, "test")));
        assertFalse(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTATTTTTTTTTTTTCGCGCGCG", 5, "test")));
    }

    @Test
    public void testHighLimitGC() {
        assertTrue(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTAGCGCGCGCGCGTTTTTTTTT", 5, "test")));
        assertTrue(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTATTTTTTTTTGCGCGCGCGCG", 5, "test")));

        assertFalse(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTAGCGCGCGCGCGCTTTTTTTT", 5, "test")));
        assertFalse(new GCContentN1N20Evaluator().evaluate(new Sequence("TTTATTTTTTTTCGCGCGCGCGCG", 5, "test")));
    }

}
