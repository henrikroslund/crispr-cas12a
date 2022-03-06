package com.henrikroslund;

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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.henrikroslund.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testIsChromosome() {
        assertTrue(isChromosomeFile("bla " + CHROMOSOME_STRING +" 1.fasta"));
        assertTrue(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2.fasta"));

        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2A.fasta"));
        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2 .fasta"));
        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +"A 2.fasta"));
        assertFalse(isChromosomeFile("something 1"));
    }

    @Test
    public void testIsPrimaryChromosomeFile() {
        assertTrue(isPrimaryChromosomeFile("bla " + CHROMOSOME_STRING +" 1.fasta"));
        assertFalse(isPrimaryChromosomeFile("bla " + CHROMOSOME_STRING +" 2.fasta"));
    }

    @Test
    public void testGetChromosomeFiles() {
        ArrayList<String> files = getChromosomeFiles("bla " + CHROMOSOME_STRING +" 1.fasta");
        assertEquals(5, files.size());
        assertEquals("bla " + CHROMOSOME_STRING +" 1.fasta", files.get(0));
        assertEquals("bla " + CHROMOSOME_STRING +" 2.fasta", files.get(1));
        assertEquals("bla " + CHROMOSOME_STRING +" 3.fasta", files.get(2));
        assertEquals("bla " + CHROMOSOME_STRING +" 4.fasta", files.get(3));
        assertEquals("bla " + CHROMOSOME_STRING +" 5.fasta", files.get(4));
    }
}
