package com.henrikroslund;

import com.henrikroslund.exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

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
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        String complement = "CTTTTGGGGGTTTTTGGGGGTAAA";
        Sequence sequence = new Sequence(originalsq, 5, "test");
        Assert.assertEquals(originalsq, sequence.getRaw());
        Assert.assertEquals(complement, sequence.getComplement().getRaw());
    }

}
