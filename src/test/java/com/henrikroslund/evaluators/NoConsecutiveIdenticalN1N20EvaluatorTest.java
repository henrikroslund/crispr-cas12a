package com.henrikroslund.evaluators;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.sequence.Sequence;
import org.junit.jupiter.api.Test;

import static com.henrikroslund.evaluators.NoConsecutiveIdenticalN1N20Evaluator.Type.*;
import static org.junit.jupiter.api.Assertions.*;

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
