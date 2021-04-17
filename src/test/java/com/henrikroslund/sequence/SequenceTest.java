package com.henrikroslund.sequence;

import com.henrikroslund.TestUtils;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.exceptions.InvalidSequenceException;
import com.henrikroslund.sequence.Sequence;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class SequenceTest {

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthShort() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5, "test");
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthLong() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5, "test");
    }

    @Test
    public void testGetComplement() throws Exception {
        Sequence sequence = new Sequence(TestUtils.VALID_CRISPR_SEQUENCE, 5, "test");
        Assert.assertEquals(TestUtils.VALID_CRISPR_SEQUENCE, sequence.getRaw());
        Assert.assertEquals(TestUtils.VALID_CRISPR_SEQUENCE_COMPLEMENT, sequence.getComplement().getRaw());
    }

    @Test
    public void testPamNotEqual() {
        Sequence sequence1 = new Sequence("CGCTATCAAGAATGTTAGTATCAA", 5, "test");
        Sequence sequence2 = new Sequence("TTTGCCTATACAAGAGGACCGGCT", 10, "test");
        Assert.assertFalse(sequence1.equalsPam(sequence2));
    }

    @Test
    public void testHashCalculations() {
        String pam = "CGCG";
        String seed = "TATATA";
        Sequence sequence1 = new Sequence(pam + seed + "AATGTTAGTATCAA", 5, "test");
        Assert.assertEquals(pam.hashCode(), sequence1.getPamHash());
        Assert.assertEquals(seed.hashCode(), sequence1.getSeedHash());
    }

    @Test
    public void testCompareTo() {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        Sequence sequence1 = new Sequence(originalsq, 5, "test");
        Sequence sequence2 = new Sequence(originalsq, 10, "test");
        Sequence sequence3 = new Sequence(originalsq, 1, "test", true);
        Assert.assertEquals(-1, sequence1.compareTo(sequence2));

        List<Sequence> list = new ArrayList<>();
        list.add(sequence3);
        list.add(sequence2);
        list.add(sequence1);
        Collections.sort(list);
        Assert.assertSame(sequence1, list.get(0));
        Assert.assertSame(sequence2, list.get(1));
        Assert.assertSame(sequence3, list.get(2));
    }

    @Test
    public void testToStringParse() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace");
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        Assert.assertEquals(sequence.getRaw(), sequence2.getRaw());
        Assert.assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        Assert.assertEquals(sequence.getComplement(), sequence2.getComplement());
        Assert.assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        Assert.assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        Assert.assertTrue(sequence.toString().compareTo(sequence2.toString()) == 0);
    }

    @Test
    public void testToStringParseWithMetaData() {
        Map<TypeEvaluator.Type, Integer> metaData = new HashMap<>();
        metaData.put(TypeEvaluator.Type.TYPE_1, 5);
        metaData.put(TypeEvaluator.Type.TYPE_2, 10);
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace", false, metaData);
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        Assert.assertEquals(sequence.getRaw(), sequence2.getRaw());
        Assert.assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        Assert.assertEquals(sequence.getComplement(), sequence2.getComplement());
        Assert.assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        Assert.assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        Assert.assertTrue(sequence.toString().compareTo(sequence2.toString()) == 0);
        Assert.assertEquals(2, sequence.getMetaData().size());
        Assert.assertEquals(Integer.valueOf(5), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_1));
        Assert.assertEquals(Integer.valueOf(10), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_2));
    }

}
