package com.henrikroslund.sequence;

/*-
 * #%L
 * crispr-cas12a
 * %%
 * Copyright (C) 2020 - 2022 Henrik Roslund
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.henrikroslund.TestUtils;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.exceptions.InvalidSequenceException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SequenceTest {

    @Test
    public void testValidSequenceLengthShort() {
        assertThrows(InvalidSequenceException.class, () -> new Sequence("TTTACCCCCAAAAACCCCCAAAA", 5, "test"));
    }

    @Test
    public void testValidSequenceLengthLong() {
        assertThrows(InvalidSequenceException.class, () -> new Sequence("TTTACCCCCAAAAACCCCCAAAAAA", 5, "test"));
    }

    @Test
    public void testGetComplement() {
        Sequence sequence = new Sequence(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, 5, "test");
        assertEquals(TestUtils.VALID_STRICT_CRISPR_SEQUENCE, sequence.getRaw());
        assertEquals(TestUtils.VALID_CRISPR_SEQUENCE_COMPLEMENT, sequence.getComplement().getRaw());
    }

    @Test
    public void testPamNotEqual() {
        Sequence sequence1 = new Sequence("CGCTATCAAGAATGTTAGTATCAA", 5, "test");
        Sequence sequence2 = new Sequence("TTTGCCTATACAAGAGGACCGGCT", 10, "test");
        assertFalse(sequence1.equalsPam(sequence2));
    }

    @Test
    public void testHashCalculations() {
        String pam = "CGCG";
        String seed = "TATATA";
        Sequence sequence1 = new Sequence(pam + seed + "AATGTTAGTATCAA", 5, "test");
        assertEquals(pam.hashCode(), sequence1.getPamHash());
        assertEquals(seed.hashCode(), sequence1.getSeedHash());
    }

    @Test
    public void testCompareTo() {
        Sequence sequence1 = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test");
        Sequence sequence2 = new Sequence("TTTACCCCCAAAAACCCCCAAAAA", 10, "test");
        Sequence sequence3 = new Sequence("TTTACCCCCAAAAACCCCCAAAAC", 1, "test", true);
        assertEquals(1, sequence1.compareTo(sequence2));

        List<Sequence> list = new ArrayList<>();
        list.add(sequence3);
        list.add(sequence2);
        list.add(sequence1);
        Collections.sort(list);
        assertSame(sequence2, list.get(0));
        assertSame(sequence3, list.get(1));
        assertSame(sequence1, list.get(2));
    }

    @Test
    public void testToStringParse() {
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace");
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        assertEquals(sequence.getRaw(), sequence2.getRaw());
        assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        assertEquals(sequence.getComplement(), sequence2.getComplement());
        assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        assertEquals(0, sequence.toString().compareTo(sequence2.toString()));
    }

    @Test
    public void testToStringParseWithMetaData() {
        Map<TypeEvaluator.Type, Integer> metaData = new HashMap<>();
        metaData.put(TypeEvaluator.Type.TYPE_1, 5);
        metaData.put(TypeEvaluator.Type.TYPE_2, 10);
        Sequence sequence = new Sequence("TTTACCCCCAAAAACCCCCAAAAG", 5, "test_with_whitespace", false, metaData);
        String sequenceToString = sequence.toString();
        Sequence sequence2 = Sequence.parseFromToString(sequenceToString);
        assertEquals(sequence.getRaw(), sequence2.getRaw());
        assertEquals(sequence.getEndIndex(), sequence2.getEndIndex());
        assertEquals(sequence.getComplement(), sequence2.getComplement());
        assertEquals(sequence.getIsComplement(), sequence2.getIsComplement());
        assertEquals(sequence.getStartIndex(), sequence2.getStartIndex());
        assertEquals(0, sequence.toString().compareTo(sequence2.toString()));
        assertEquals(2, sequence.getMetaData().size());
        assertEquals(Integer.valueOf(5), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_1));
        assertEquals(Integer.valueOf(10), sequence.getMetaData().get(TypeEvaluator.Type.TYPE_2));
    }

    @Test
    public void testRawHashCollision() {
        String raw1 = "TTTCAATGGTCGCGAACCCACCGT";
        String raw2 = "TTTTAAATTTTCTCACCACAAAAT";
        Sequence sequence1 = new Sequence(raw1, 0, "genomeName");
        Sequence sequence2 = new Sequence(raw2, 0, "genomeNmae");
        assertEquals(raw1.hashCode(), raw2.hashCode());
        assertNotEquals(sequence1, sequence2);
    }

    @Test
    @Disabled
    // This is a test to evaluate hash collisions
    public void testSequenceHash() {
        char[] proteins = {'T', 'A', 'C', 'G'};
        ArrayList<String> allCombinationsRaw = new ArrayList<>();
        collectAllCombinations(proteins, "", proteins.length, Sequence.RAW_LENGTH, allCombinationsRaw);
        System.out.println("Total combinations: " + allCombinationsRaw.size());
        ArrayList<Sequence> allCombinations = new ArrayList<>();
        allCombinationsRaw.forEach(raw -> allCombinations.add(new Sequence(raw, 0, "")));

        AtomicInteger collisions = new AtomicInteger();

        allCombinations.parallelStream().forEach(combination -> {
            allCombinations.forEach(match -> {
                boolean isHashEqual = combination.getRaw().hashCode() == match.getRaw().hashCode();
                boolean isEqual = combination.equals(match);
                if(isHashEqual && !combination.getRaw().equalsIgnoreCase(match.getRaw())) {
                    System.out.println("Collisions found " + collisions.incrementAndGet());
                    if(isEqual) {
                        System.out.println("Warning real collision found");
                        fail("Warning real collision found");
                    }
                }
            });
        });
        assertEquals(0, collisions.get());
    }

    @Test
    public void testNoPamAndSeedHashCollisions() {
        char[] proteins = {'T', 'A', 'C', 'G'};

        int maxStringLength = 8;
        assertTrue(Sequence.PAM_LENGTH <= maxStringLength);
        assertTrue(Sequence.SEED_LENGTH <= maxStringLength);

        ArrayList<String> allCombinations = new ArrayList<>();
        collectAllCombinations(proteins, "", proteins.length, maxStringLength, allCombinations);
        AtomicInteger collisions = new AtomicInteger();
        System.out.println("Total combinations: " + allCombinations.size());
        allCombinations.parallelStream().forEach(combination -> {
            allCombinations.forEach(match -> {
                boolean isHashEqual = combination.hashCode() == match.hashCode();
                if(isHashEqual && !combination.equalsIgnoreCase(match)) {
                    fail("Collision found");
                }
            });
        });
        assertEquals(0, collisions.get());
    }

    static void collectAllCombinations(char[] set, String prefix, int n, int k, ArrayList<String> allCombinations)
    {
        if (k == 0)
        {
            allCombinations.add(prefix);
            return;
        }
        for (int i = 0; i < n; ++i)
        {
            String newPrefix = prefix + set[i];
            collectAllCombinations(set, newPrefix,
                    n, k - 1, allCombinations);
        }
    }
}
