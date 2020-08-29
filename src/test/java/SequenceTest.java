import exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

public class SequenceTest {

    @Test
    public void testValidSequence() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 5);
        Assert.assertTrue(sequence.isSeedMatch());
        Assert.assertTrue(sequence.isPamMatch());
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
    public void testMismatchSeed() {
        Sequence sequence = new Sequence("TTATCCCCCAAAAACCCCCAAAAA", 5);
        Assert.assertFalse(sequence.isSeedMatch());
        Assert.assertTrue(sequence.isPamMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testPamMatchTooFew() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCAAAAAAA", 5);
        Assert.assertTrue(sequence.isSeedMatch());
        Assert.assertFalse(sequence.isPamMatch());
        Assert.assertFalse(sequence.isValid());
    }

    @Test
    public void testPamMatchTooMany() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAACC", 5);
        Assert.assertTrue(sequence.isSeedMatch());
        Assert.assertFalse(sequence.isPamMatch());
        Assert.assertFalse(sequence.isValid());
    }
}
