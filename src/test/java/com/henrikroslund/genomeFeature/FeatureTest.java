package com.henrikroslund.genomeFeature;

import com.henrikroslund.sequence.Sequence;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FeatureTest {

    @Test
    public void testIsMatchBounds() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 80, "");
        assertTrue(feature.isMatch(sequence));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 201, "");
        assertFalse(feature.isMatch(sequence));

        feature = new Feature(100,200,"", new ArrayList<>());
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 75, "");
        assertFalse(feature.isMatch(sequence));
    }

    @Test
    public void testisMatchComplement() {
        Feature feature = new Feature(100,200,"", new ArrayList<>());
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "");
        assertTrue(feature.isMatch(sequence));
        sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 200, "", true);
        assertFalse(feature.isMatch(sequence));
    }
}