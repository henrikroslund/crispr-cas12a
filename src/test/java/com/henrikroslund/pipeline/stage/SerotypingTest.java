package com.henrikroslund.pipeline.stage;

import com.henrikroslund.pcr.Serotype;
import com.henrikroslund.pipeline.Pipeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class SerotypingTest {

    @Test
    public void testFindCorrectPCR() throws Exception {
        // Genome1 PrimerA - 922-945
        // Genome1 PrimerB - 1395-1430
        // Genome2 PrimerA - 3718-3741
        // Genome2 PrimerB - 3233-3268
        // Genome3 PrimerA - 922-945
        // Genome3 PrimerB - 1393-1428
        Serotype serotype = new Serotype("GACTACAAGGACGACGATGACAAG", "CCCCTCAAGACCCGTTTAGAGGCCCCAAGGGGTTAT", "test");
        Pipeline pipeline = new Pipeline("test", "src/test/resources/serotype_stage_test", "target/tmp");
        Serotyping serotyping = new Serotyping(List.of(serotype));
        pipeline.addStage(serotyping);
        pipeline.run();
        assertEquals(3, serotyping.getPcrProducts().size());
        assertEquals(509, serotyping.getPcrProducts().get(0).getDistance());
        assertEquals(1, serotyping.getPcrProducts().get(0).getPrimerAPositions().size());
        assertEquals(922, serotyping.getPcrProducts().get(0).getPrimerAPositions().get(0).intValue());
        assertEquals(1395, serotyping.getPcrProducts().get(0).getPrimerBPositions().get(0).intValue());
        assertEquals(509, serotyping.getPcrProducts().get(1).getDistance());
        assertEquals(1, serotyping.getPcrProducts().get(1).getPrimerAPositions().size());
        assertEquals(3718, serotyping.getPcrProducts().get(1).getPrimerAPositions().get(0).intValue());
        assertEquals(3233, serotyping.getPcrProducts().get(1).getPrimerBPositions().get(0).intValue());
        assertEquals(507, serotyping.getPcrProducts().get(2).getDistance());
        assertEquals(1, serotyping.getPcrProducts().get(2).getPrimerAPositions().size());
        assertEquals(922, serotyping.getPcrProducts().get(2).getPrimerAPositions().get(0).intValue());
        assertEquals(1393, serotyping.getPcrProducts().get(2).getPrimerBPositions().get(0).intValue());
    }

    @Test
    public void testFindCorrectPCRCircular() throws Exception {
        // Genome1 PrimerA - 1395-1430
        // Genome1 PrimerB - 2524-2552
        Serotype serotype = new Serotype("CCCCTCAAGACCCGTTTAGAGGCCCCAAGGGGTTAT", "ATTGTCTCATGAGCGGATACATATTTGAA", "test");
        Pipeline pipeline = new Pipeline("test", "src/test/resources/serotype_stage_test_circular", "target/tmp");
        Serotyping serotyping = new Serotyping(List.of(serotype));
        pipeline.addStage(serotyping);
        pipeline.run();
        assertEquals(1, serotyping.getPcrProducts().size());
        assertEquals(3504, serotyping.getPcrProducts().get(0).getDistance());
    }

    @Test
    public void testFindOccurrence() {
        String genomeSequenceData = "ABCDEFGDE";
        String sequence = "DE";
        List<Integer> indexes = Serotyping.findOccurrences(genomeSequenceData, sequence, false, "test");
        assertEquals(2, indexes.size());
        assertEquals(4, indexes.get(0).intValue());
        assertEquals(8, indexes.get(1).intValue());
    }

    @Test
    public void testFindOccurrenceComplement() {
        String genomeSequenceData = "ABCDEFGDE";
        String sequence = "DE";
        List<Integer> indexes = Serotyping.findOccurrences(genomeSequenceData, sequence, true, "test");
        assertEquals(2, indexes.size());
        assertEquals(5, indexes.get(0).intValue());
        assertEquals(1, indexes.get(1).intValue());
    }
}
