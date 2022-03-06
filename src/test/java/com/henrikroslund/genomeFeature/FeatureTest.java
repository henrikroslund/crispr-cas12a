package com.henrikroslund.genomeFeature;

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
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureTest {

    @Test
    public void testIsMatchBounds() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 80, "");
        assertTrue(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 201, "");
        assertFalse(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 75, "");
        assertFalse(feature.isMatch(sequence, true));
    }

    @Test
    public void testisMatchComplement() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence, true));
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "", true);
        assertFalse(feature.isMatch(sequence, true));
        assertTrue(feature.isMatch(sequence, false));
    }
}
