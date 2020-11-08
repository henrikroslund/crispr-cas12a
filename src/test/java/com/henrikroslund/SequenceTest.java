package com.henrikroslund;

import com.henrikroslund.exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

public class SequenceTest {

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthShort() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5);
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthLong() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5);
    }

    @Test
    public void testGetComplement() throws Exception {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        String complement = "CTTTTGGGGGTTTTTGGGGGTAAA";
        Sequence sequence = new Sequence(originalsq, 5);
        Assert.assertEquals(originalsq, sequence.getRaw());
        Assert.assertEquals(complement, sequence.getComplement().getRaw());
    }

}
