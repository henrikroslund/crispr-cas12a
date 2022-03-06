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
