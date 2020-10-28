import exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

public class SequenceTest {

    @Test
    public void testValidSequence() {
        Sequence sequence = new Sequence("TTTAGTGAGGACTCCTTCATCGTG", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertTrue(sequence.isTargetMatch());
        Assert.assertTrue(sequence.isValid());
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthShort() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5);
    }

    @Test(expected = InvalidSequenceException.class)
    public void testValidSequenceLengthLong() {
        new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5);
    }

    @Test
    public void testMismatchPam() {
        Sequence sequence = new Sequence("TATAGTGAGGACTCCTTCATCGTG", 5);
        Assert.assertFalse(sequence.isPamMatch());
        Assert.assertTrue(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testTargetMatchTooFew() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCAAAAAAA", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertFalse(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testTargetMatchTooMany() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAACC", 5);
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertFalse(sequence.isTargetMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testGetComplement() throws Exception {
        String originalsq = "TTTACCCCCAAAAACCCCCAAAAG";
        String complement = "CTTTTGGGGGTTTTTGGGGGTAAA";
        Sequence sequence = new Sequence(originalsq, 5);
        Assert.assertEquals(sequence.getRaw(), originalsq);
        Assert.assertEquals(sequence.getComplement().getRaw(), complement);
    }

    @Test
    public void testIsPartialMatchPamDifferent() {
        String originalSequence =  "TTTACCCCCAAAAACCCCCAAAAA";
        String compareToSequence = "TTTCAAAAACCCCCAAAAACCCCC";
        Sequence original = new Sequence(originalSequence, 5);
        Sequence compareTo = new Sequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchIdentical() {
        String originalSequence =  "TTTACCCCCAAAAACCCCCAAAAA";
        String compareToSequence = "TTTACCCCCAAAAACCCCCAAAAA";
        Sequence original = new Sequence(originalSequence, 5);
        Sequence compareTo = new Sequence(compareToSequence, 5);
        Assert.assertTrue(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamOneConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCACCCCCCCCCCCCCCCCC";
        Sequence original = new Sequence(originalSequence, 5);
        Sequence compareTo = new Sequence(compareToSequence, 5);
        Assert.assertTrue(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamTwoConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCAACCCCCCCCCCCCCCCC";
        Sequence original = new Sequence(originalSequence, 5);
        Sequence compareTo = new Sequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }

    @Test
    public void testIsPartialMatchSamePamThreeConsecutiveMismatch() {
        String originalSequence =  "TTTACCCCCCCCCCCCCCCCCCCC";
        String compareToSequence = "TTTACCCAAACCCCCCCCCCCCCC";
        Sequence original = new Sequence(originalSequence, 5);
        Sequence compareTo = new Sequence(compareToSequence, 5);
        Assert.assertFalse(original.isPartialMatch(compareTo));
    }
}
