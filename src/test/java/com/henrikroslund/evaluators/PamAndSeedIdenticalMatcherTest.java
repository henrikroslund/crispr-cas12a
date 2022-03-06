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
