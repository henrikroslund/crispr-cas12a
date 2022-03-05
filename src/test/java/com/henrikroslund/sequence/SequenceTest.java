package com.henrikroslund.sequence;

import com.henrikroslund.TestUtils;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.exceptions.InvalidSequenceException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SequenceTest {

    @Test
    public void testValidSequenceLengthShort() {
        assertThrows(InvalidSequenceException.class, () -> new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5, "test"));
    }

    @Test
    public void testValidSequenceLengthLong() {
        assertThrows(InvalidSequenceException.class, () -> new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5, "test"));
    }

    @Test
    public void testGetComplement() {
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 5, "test");
        assertEquals(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, sequence.getRaw());
        assertEquals(TestUtils.VALID_CRISPR_SEQUENCE_COMPLEMENT, sequence.getComplement().getRaw());
    }

    @Test
    public void testPamNotEqual() {
        Sequence sequence1 = new Sequence("CGCTATCAAGAATGTTAGTATCAA", 5, "test");
        Sequence sequence2 = new Sequence("TTTGCCTATACAAGAGGACCGGCT", 10, "test");
        assertFalse(sequence1.equalsPam(sequence2));
    }

    @Test
    public void testHashCalculations() {
        String pam = "CGCG";
        String seed = "TATATA";
        Sequence sequence1 = new Sequence(pam + seed + "AATGTTAGTATCAA", 5, "test");
        assertEquals(pam.hashCode(), sequence1.getPamHash());
        assertEquals(seed.hashCode(), sequence1.getSeedHash());
    }

    @Test
    public void testCompareTo() {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        Sequence sequence1 = new Sequence(originalsq, 5, "test");
        Sequence sequence2 = new Sequence(originalsq, 10, "test");
        Sequence sequence3 = new Sequence(originalsq, 1, "test", true);
        assertEquals(-1, sequence1.compareTo(sequence2));

        List<Sequence> list = new ArrayList<>();
        list.add(sequence3);
        list.add(sequence2);
        list.add(sequence1);
        Collections.sort(list);
        assertSame(sequence1, list.get(0));
        assertSame(sequence2, list.get(1));
        assertSame(sequence3, list.get(2));
    }

    @Test
    public void testToStringParse() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace");
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        assertEquals(sequence.getRaw(), sequence2.getRaw());
        assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        assertEquals(sequence.getComplement(), sequence2.getComplement());
        assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        assertEquals(0, sequence.toString().compareTo(sequence2.toString()));
    }

    @Test
    public void testToStringParseWithMetaData() {
        Map<TypeEvaluator.Type, Integer> metaData = new HashMap<>();
        metaData.put(TypeEvaluator.Type.TYPE_1, 5);
        metaData.put(TypeEvaluator.Type.TYPE_2, 10);
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace", false, metaData);
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        assertEquals(sequence.getRaw(), sequence2.getRaw());
        assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        assertEquals(sequence.getComplement(), sequence2.getComplement());
        assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        assertEquals(0, sequence.toString().compareTo(sequence2.toString()));
        assertEquals(2, sequence.getMetaData().size());
        assertEquals(Integer.valueOf(5), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_1));
        assertEquals(Integer.valueOf(10), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_2));
    }

}
