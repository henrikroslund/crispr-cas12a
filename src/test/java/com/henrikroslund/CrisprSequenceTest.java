package com.henrikroslund;

import com.henrikroslund.exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

public class CrisprSequenceTest {

    @Test
    public void testValidSequence() {
        CrisprSequence sequence = new CrisprSequence("TTTAGTGAGGACTCCTTCATCGTG", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertTrue(sequence.isTargetMatch());
        Assert.assertTrue(sequence.isValid());
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthShort() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5, "test");
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthLong() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5, "test");
    }

    @Test
    public void testMismatchPam() {
        CrisprSequence sequence = new CrisprSequence("TATAGTGAGGACTCCTTCATCGTG", 5);
        Assert.assertFalse(sequence.isPamMatch());
        Assert.assertTrue(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testTargetMatchTooFew() {
        CrisprSequence sequence = new CrisprSequence("TTTACCCCCAAAAACCCAAAAAAA", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertFalse(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testTargetMatchTooMany() {
        CrisprSequence sequence = new CrisprSequence("TTTACCCCCAAAAACCCCCAAACC", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertFalse(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testGetComplement() throws Exception {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        String complement = "CTTTTGGGGGTTTTTGGGGGTAAA";
        CrisprSequence sequence = new CrisprSequence(originalsq, 5);
        Assert.assertEquals(sequence.getRaw(), originalsq);
        Assert.assertEquals(sequence.getComplement().getRaw(), complement);
    }

    @Test
    public void testIsPartialMatchPamDifferent() {
        String originalSequence =  "TTTACCCCCAAAAACCCCCAAAAA";
        String compareToSequence = "TTTCAAAAACCCCCAAAAACCCCC";
        CrisprSequence original = new CrisprSequence(originalSequence, 5);
        CrisprSequence compareTo = new CrisprSequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchIdentical() {
        String originalSequence =  "TTTACCCCCAAAAACCCCCAAAAA";
        String compareToSequence = "TTTACCCCCAAAAACCCCCAAAAA";
        CrisprSequence original = new CrisprSequence(originalSequence, 5);
        CrisprSequence compareTo = new CrisprSequence(compareToSequence, 5);
        Assert.assertTrue(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamOneConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCACCCCCCCCCCCCCCCCC";
        CrisprSequence original = new CrisprSequence(originalSequence, 5);
        CrisprSequence compareTo = new CrisprSequence(compareToSequence, 5);
        Assert.assertTrue(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamTwoConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCAACCCCCCCCCCCCCCCC";
        CrisprSequence original = new CrisprSequence(originalSequence, 5);
        CrisprSequence compareTo = new CrisprSequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamThreeConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCCAAACCCCCCCCCCCCCC";
        CrisprSequence original = new CrisprSequence(originalSequence, 5);
        CrisprSequence compareTo = new CrisprSequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }
}
