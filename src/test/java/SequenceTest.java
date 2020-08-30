import exceptions.InvalidSequenceException;
import org.junit.Assert;
import org.junit.Test;

public class SequenceTest {

    @Test
    public void testValidSequence() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 5);
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
        Sequence sequence = new Sequence("TTATCCCCCAAAAACCCCCAAAAA", 5);
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
        String complement = "AAATGGGGGTTTTTGGGGGTTTTC";
        Sequence sequence = new Sequence(originalsq, 5);
        Assert.assertEquals(sequence.getRaw(), originalsq);
        Assert.assertEquals(sequence.getComplement().getRaw(), complement);
    }
}
