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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoverageAnalysisTest {

    @Test
    public void getCoverageMapMultipleSequencesTwoSequences() {
        Map<Sequence, TreeSet<String>> coverage = Collections.synchronizedMap(new TreeMap<>());

        Sequence sequence1 = new Sequence("TTTACCCCCAAAAACCCCCAAATG", 0, "test1");
        String genome1 = "genome1";
        TreeSet<String> sequence1Genomes = new TreeSet<>();
        sequence1Genomes.add(genome1);

        Sequence sequence2 = new Sequence("TTTAAAAAAAAAAAACCCCAAATG", 0, "test2");
        String genome2 = "genome2";
        TreeSet<String> sequence2Genomes = new TreeSet<>();
        sequence2Genomes.add(genome2);

        coverage.put(sequence1,sequence1Genomes);
        coverage.put(sequence2,sequence2Genomes);
        TreeSet<Pair<List<Sequence>, HashSet<String>>> coverageMultiple = CoverageAnalysis.getCoverageMapMultipleSequences(coverage);

        assertEquals(1, coverageMultiple.size());

        HashSet<String> genomes = coverageMultiple.stream().findFirst().get().getRight();
        assertEquals(2, genomes.size());
        assertTrue(genomes.contains(genome1));
        assertTrue(genomes.contains(genome2));
    }

    @Test
    public void getCoverageMapMultipleSequencesThreeSequences() {
        Map<Sequence, TreeSet<String>> coverage = Collections.synchronizedMap(new TreeMap<>());

        Sequence sequence1 = new Sequence("TTTACCCCCAAAAACCCCCAAATG", 0, "test1");
        String genome1 = "genome1";
        TreeSet<String> sequence1Genomes = new TreeSet<>();
        sequence1Genomes.add(genome1);

        Sequence sequence2 = new Sequence("TTTAAAAAAAAAAAACCCCAAATG", 0, "test2");
        String genome2 = "genome2";
        TreeSet<String> sequence2Genomes = new TreeSet<>();
        sequence2Genomes.add(genome2);

        Sequence sequence3 = new Sequence("TTTCCCCAAAAAAAACCCCAAATG", 0, "test3");
        TreeSet<String> sequence3Genomes = new TreeSet<>();
        sequence2Genomes.add(genome2);

        coverage.put(sequence1,sequence1Genomes);
        coverage.put(sequence2,sequence2Genomes);
        coverage.put(sequence3,sequence3Genomes);
        TreeSet<Pair<List<Sequence>, HashSet<String>>> coverageMultiple = CoverageAnalysis.getCoverageMapMultipleSequences(coverage);

        assertEquals(3, coverageMultiple.size());

        Iterator<Pair<List<Sequence>, HashSet<String>>> it = coverageMultiple.iterator();
        HashSet<String> genomes = it.next().getRight();
        assertEquals(2, genomes.size());
        assertTrue(genomes.contains(genome1));
        assertTrue(genomes.contains(genome2));

        genomes = it.next().getRight();
        assertEquals(1, genomes.size());
        assertTrue(genomes.contains(genome2));
    }
}
