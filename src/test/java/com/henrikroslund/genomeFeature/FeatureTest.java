package com.henrikroslund.genomeFeature;

import com.henrikroslund.sequence.Sequence;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureTest {

    @Test
    public void testIsMatchBounds() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 80, "");
        assertTrue(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 201, "");
        assertFalse(feature.isMatch(sequence, true));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 75, "");
        assertFalse(feature.isMatch(sequence, true));
    }

    @Test
    public void testisMatchComplement() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence, true));
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "", true);
        assertFalse(feature.isMatch(sequence, true));
        assertTrue(feature.isMatch(sequence, false));
    }
}
