package com.henrikroslund;

import com.henrikroslund.evaluators.CrisprPamEvaluator;
import com.henrikroslund.evaluators.IdenticalEvaluator;
import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class GenomeTest {

    @Test
    public void testGenomeConstructorNoFile() {
        String filename = "someFile.fasta";
        Genome genome = new Genome(true, filename, "firstRow");
        assertTrue(genome.getSequences().isEmpty());
        assertEquals(0, genome.getTotalSequences());
        assertEquals("someFile", genome.getFilename());
    }

    @Test
    public void testGenomeConstructorIncludeAllChromosomes() throws Exception {
        File chromosome1 = new File("src/test/resources/chromosomes/genome chromosome 1.fasta");
        Genome genome = new Genome(chromosome1, Collections.emptyList(), false, false);
        assertEquals(2, genome.getTotalSequences());

        File chromosome2 = new File("src/test/resources/chromosomes/genome chromosome 2.fasta");
        genome = new Genome(chromosome2, Collections.emptyList(), false, false);
        assertEquals(2, genome.getTotalSequences());

        genome = new Genome(chromosome1, Collections.emptyList(), false, true);
        assertEquals(4, genome.getTotalSequences());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenomeConstructorIncludeAllChromosomesAttemptToCreateForWrongChromosome() throws Exception {
        File chromosome2 = new File("src/test/resources/chromosomes/genome chromosome 2.fasta");
        new Genome(chromosome2, Collections.emptyList(), false, true);
    }

    @Test(expected = IOException.class)
    public void testFileNotExists() throws Exception {
        File file = new File("randomFileWhichDoesNotExist");
        new Genome(file, Collections.emptyList(), false, false);
    }

    @Test
    public void testCreateSequencesSingleSequence() {
        for(Boolean skipDuplicates : Arrays.asList(true, false)) {
            Genome genome = new Genome(skipDuplicates, "filename", "firstRow");
            genome.createSequences(Collections.emptyList(), TestUtils.VALID_STRICT_CRISPR_SEQUENCE);
            assertEquals(2, genome.getTotalSequences());

            Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
            assertTrue(genome.getSequences().contains(sequence));
            Sequence match = genome.getSequenceMatchingAllEvaluators(Collections.singletonList(new IdenticalEvaluator(sequence)));
            assertEquals(0, sequence.compareTo(match));
            assertNotSame(sequence, match);
            if(skipDuplicates) {
                assertTrue(genome.exists(sequence));
            }

            sequence = new Sequence(TestUtils.VALID_CRISPR_SEQUENCE_COMPLEMENT, 23, "filename", true);
            assertTrue(genome.getSequences().contains(sequence));
            match = genome.getSequenceMatchingAllEvaluators(Collections.singletonList(new IdenticalEvaluator(sequence)));
            assertEquals(0, sequence.compareTo(match));
            assertNotSame(sequence, match);
            if(skipDuplicates) {
                assertTrue(genome.exists(sequence));
            }
        }
    }

    @Test
    public void testGetSequenceMatchingAllEvaluators() {
        Genome genome = new Genome(true, "filename", "firstRow");
        genome.createSequences(Collections.emptyList(), "TTTTTTTTTTTTTTTTTTTTTTTTT");
        assertEquals(2, genome.getTotalSequences());

        Sequence sequence = genome.getSequenceMatchingAllEvaluators(Collections.emptyList());
        assertNotNull(sequence);
        assertNotNull(genome.getSequenceMatchingAllEvaluators(
                Collections.singletonList(new IdenticalEvaluator(sequence))));

        assertNull(genome.getSequenceMatchingAllEvaluators(
                Collections.singletonList(new CrisprPamEvaluator(true))));

        assertNull(genome.getSequenceMatchingAllEvaluators(
                Arrays.asList(new IdenticalEvaluator(sequence), new CrisprPamEvaluator(true))));

        assertNull(genome.getSequenceMatchingAllEvaluators(
                Arrays.asList(new CrisprPamEvaluator(true), new IdenticalEvaluator(sequence))));
    }

    @Test
    public void testGetSequenceMatchingAnyEvaluators() {
        Genome genome = new Genome(true, "filename", "firstRow");
        genome.createSequences(Collections.emptyList(), "TTTTTTTTTTTTTTTTTTTTTTTTT");
        assertEquals(2, genome.getTotalSequences());

        List<Sequence> sequences = genome.getSequencesMatchingAnyEvaluator(Collections.emptyList());
        assertEquals(0, sequences.size());

        sequences = genome.getSequencesMatchingAnyEvaluator(
                Collections.singletonList(new IdenticalEvaluator(genome.getSequences().stream().findFirst().get())));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        assertEquals(0, sequence.compareTo(sequences.get(0)));

        sequences = genome.getSequencesMatchingAnyEvaluator(
                Collections.singletonList(new CrisprPamEvaluator(true)));
        assertTrue(sequences.isEmpty());

        sequences = genome.getSequencesMatchingAnyEvaluator(
                Arrays.asList(new IdenticalEvaluator(sequence), new CrisprPamEvaluator(true)));
        assertEquals(1, sequences.size());
        assertEquals(0, sequence.compareTo(sequences.get(0)));

        sequences = genome.getSequencesMatchingAnyEvaluator(
                Arrays.asList(new CrisprPamEvaluator(true), new IdenticalEvaluator(sequence)));
        assertEquals(1, sequences.size());
        assertEquals(0, sequence.compareTo(sequences.get(0)));
    }

    @Test
    public void testCreateSequencesWithSkipDuplicates() {
        Genome genome = new Genome(true, "filename", "firstRow");
        genome.createSequences(Collections.emptyList(), "TTTTTTTTTTTTTTTTTTTTTTTTT");
        assertEquals(2, genome.getTotalSequences());

        genome = new Genome(false, "filename", "firstRow");
        genome.createSequences(Collections.emptyList(), "TTTTTTTTTTTTTTTTTTTTTTTTT");
        assertEquals(4, genome.getTotalSequences());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExistsSkipDuplicates() {
        Genome genome = new Genome(false, "filename", "firstRow");
        genome.createSequences(Collections.emptyList(), TestUtils.VALID_STRICT_CRISPR_SEQUENCE);
        assertEquals(2, genome.getTotalSequences());

        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
        genome.exists(sequence);
    }

    @Test
    public void testAddAllWithSkipDuplicates() {
        Genome genome = new Genome(true, "filename", "firstRow");
        assertTrue(genome.getSequences().isEmpty());
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(1, genome.getTotalSequences());
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(1, genome.getTotalSequences());
    }

    @Test
    public void testAddAllWithDuplicates() {
        Genome genome = new Genome(false, "filename", "firstRow");
        assertTrue(genome.getSequences().isEmpty());
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(1, genome.getTotalSequences());
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(2, genome.getTotalSequences());
    }

    @Test
    public void testRemoveAllWithSkipDuplicates() {
        Genome genome = new Genome(true, "filename", "firstRow");
        assertTrue(genome.getSequences().isEmpty());
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(1, genome.getTotalSequences());
        genome.removeAll(Collections.singletonList(sequence));
        assertTrue(genome.getSequences().isEmpty());
    }

    @Test
    public void testRemoveAllWithDuplicates() {
        Genome genome = new Genome(false, "filename", "firstRow");
        assertTrue(genome.getSequences().isEmpty());
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 0, "filename");
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(1, genome.getTotalSequences());
        genome.addAll(Collections.singletonList(sequence));
        assertEquals(2, genome.getTotalSequences());
        genome.removeAll(Collections.singletonList(sequence));
        assertTrue(genome.getSequences().isEmpty());
    }
}