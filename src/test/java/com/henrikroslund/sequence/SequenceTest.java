package com.henrikroslund.sequence;

import com.henrikroslund.TestUtils;
import com.henrikroslund.exceptions.InvalidSequenceException;
import com.henrikroslund.sequence.Sequence;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        Assert.assertEquals(pam.hashCode(), sequence1.pamHash);
        Assert.assertEquals(seed.hashCode(), sequence1.seedHash);
    }

    @Test
    public void testCompareTo() {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        Sequence sequence1 = new Sequence(originalsq, 5, "test");
        Sequence sequence2 = new Sequence(originalsq, 10, "test");
        Sequence sequence3 = new Sequence(originalsq, 1, "test");
        sequence3.setComplement(true);
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

}
