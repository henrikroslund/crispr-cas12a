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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
