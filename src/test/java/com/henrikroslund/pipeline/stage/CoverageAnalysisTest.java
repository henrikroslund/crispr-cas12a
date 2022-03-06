package com.henrikroslund.pipeline.stage;

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
