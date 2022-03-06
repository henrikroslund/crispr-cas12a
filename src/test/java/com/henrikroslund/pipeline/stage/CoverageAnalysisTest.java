package com.henrikroslund.pipeline.stage;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CoverageAnalysisTest {

    @Test
    public void getCoverageMapMultipleSequencesTwoSequences() {
        Map<Sequence, HashSet<String>> coverage = new ConcurrentHashMap<>();

        Sequence sequence1 = new Sequence("TTTACCCCCAAAAACCCCCAAATG", 0, "test1");
        String genome1 = "genome1";
        HashSet<String> sequence1Genomes = new HashSet<>();
        sequence1Genomes.add(genome1);

        Sequence sequence2 = new Sequence("TTTAAAAAAAAAAAACCCCAAATG", 0, "test2");
        String genome2 = "genome2";
        HashSet<String> sequence2Genomes = new HashSet<>();
        sequence2Genomes.add(genome2);

        coverage.put(sequence1,sequence1Genomes);
        coverage.put(sequence2,sequence2Genomes);
        Map<List<Sequence>, HashSet<String>> coverageMultiple = CoverageAnalysis.getCoverageMapMultipleSequences(coverage);

        assertEquals(1, coverageMultiple.size());

        List<Sequence> sequences = new ArrayList<>();
        sequences.add(sequence1);
        sequences.add(sequence2);
        Collections.sort(sequences);
        HashSet<String> genomes = coverageMultiple.get(sequences);
        assertEquals(2, genomes.size());
        assertTrue(genomes.contains(genome1));
        assertTrue(genomes.contains(genome2));
    }

    @Test
    public void getCoverageMapMultipleSequencesThreeSequences() {
        Map<Sequence, HashSet<String>> coverage = new ConcurrentHashMap<>();

        Sequence sequence1 = new Sequence("TTTACCCCCAAAAACCCCCAAATG", 0, "test1");
        String genome1 = "genome1";
        HashSet<String> sequence1Genomes = new HashSet<>();
        sequence1Genomes.add(genome1);

        Sequence sequence2 = new Sequence("TTTAAAAAAAAAAAACCCCAAATG", 0, "test2");
        String genome2 = "genome2";
        HashSet<String> sequence2Genomes = new HashSet<>();
        sequence2Genomes.add(genome2);

        Sequence sequence3 = new Sequence("TTTCCCCAAAAAAAACCCCAAATG", 0, "test3");
        HashSet<String> sequence3Genomes = new HashSet<>();
        sequence2Genomes.add(genome2);

        coverage.put(sequence1,sequence1Genomes);
        coverage.put(sequence2,sequence2Genomes);
        coverage.put(sequence3,sequence3Genomes);
        Map<List<Sequence>, HashSet<String>> coverageMultiple = CoverageAnalysis.getCoverageMapMultipleSequences(coverage);

        assertEquals(3, coverageMultiple.size());

        List<Sequence> sequences = new ArrayList<>();
        sequences.add(sequence1);
        sequences.add(sequence2);
        Collections.sort(sequences);
        HashSet<String> genomes = coverageMultiple.get(sequences);
        assertEquals(2, genomes.size());
        assertTrue(genomes.contains(genome1));
        assertTrue(genomes.contains(genome2));

        sequences = new ArrayList<>();
        sequences.add(sequence2);
        sequences.add(sequence3);
        Collections.sort(sequences);
        genomes = coverageMultiple.get(sequences);
        assertEquals(1, genomes.size());
        assertTrue(genomes.contains(genome2));
    }
}
