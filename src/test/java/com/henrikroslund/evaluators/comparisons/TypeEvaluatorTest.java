package com.henrikroslund.evaluators.comparisons;

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

import com.henrikroslund.sequence.Sequence;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeEvaluatorTest {

    /*
    TYPE_1, // >=X mismatches in PAM's first 3
    TYPE_2, // >=X consecutive mismatches in Seed
    TYPE_3, // If Type_1 & Type_2
    TYPE_4, // Did not bind in genome. Cannot be evaluated in this class on a sequence comparison level.
    TYPE_5, // >=X mismatches in N7-N20
    TYPE_6, // >=X mismatches in Seed
    TYPE_DISCARD // If no other type applies
     */
    final List<TypeEvaluator.Type> discard = Collections.singletonList(TypeEvaluator.Type.TYPE_DISCARD);

    @Test
    public void testType1() {
        List<TypeEvaluator.Type> type__1 = Collections.singletonList(TypeEvaluator.Type.TYPE_1);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 0, 99, 99, 99, 99, 99);

        assertEquals(type__1, evaluate(typeEvaluator, "TTTTAAAAAATTTTTTTTTTTTTT"));

        typeEvaluator = new TypeEvaluator(sequence, 1, 99, 99, 99, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "TTTTAAAAAATTTTTTTTTTTTTT"));
        assertEquals(type__1, evaluate(typeEvaluator, "ATTTAAAAAATTTTTTTTTTTTTT"));
        assertEquals(type__1, evaluate(typeEvaluator, "TATTAAAAAATTTTTTTTTTTTTT"));
        assertEquals(type__1, evaluate(typeEvaluator, "TTATAAAAAATTTTTTTTTTTTTT"));
        assertEquals(type__1, evaluate(typeEvaluator, "AAATAAAAAATTTTTTTTTTTTTT"));
        assertEquals(discard, evaluate(typeEvaluator, "TTTAAAAAAATTTTTTTTTTTTTT"));
    }

    @Test
    public void testType2() {
        List<TypeEvaluator.Type> type__2 = Collections.singletonList(TypeEvaluator.Type.TYPE_2);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 99, 0, 99, 99, 99, 99);

        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTTTTAAAAAAAAAAAAAA"));

        typeEvaluator = new TypeEvaluator(sequence, 99, 1, 99, 99, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "AAAATTTTTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAAATTTTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATATTTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTATTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTATTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTTATAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTTTAAAAAAAAAAAAAAA"));

        typeEvaluator = new TypeEvaluator(sequence, 99, 2, 99, 99, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "AAAATTTTTTAAAAAAAAAAAAAA"));
        assertEquals(discard, evaluate(typeEvaluator, "AAAAATTTTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAAAATTTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATAATTTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTAATTAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTAATAAAAAAAAAAAAAA"));
        assertEquals(type__2, evaluate(typeEvaluator, "AAAATTTTAAAAAAAAAAAAAAAA"));
    }

    @Test
    public void testType3() {
        List<TypeEvaluator.Type> type_23 = Arrays.asList(TypeEvaluator.Type.TYPE_1, TypeEvaluator.Type.TYPE_2, TypeEvaluator.Type.TYPE_3);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 0, 0, 99, 99, 99, 99);

        assertEquals(type_23, evaluate(typeEvaluator, "TTTTTTTTTTAAAAAAAAAAAAAA"));

        typeEvaluator = new TypeEvaluator(sequence, 1, 1, 99, 99, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "TTTTTTTTTTAAAAAAAAAAAAAA"));
        assertEquals(type_23, evaluate(typeEvaluator, "ATTTATTTTTAAAAAAAAAAAAAA"));
        assertEquals(type_23, evaluate(typeEvaluator, "TATTTTATTTAAAAAAAAAAAAAA"));
        assertEquals(type_23, evaluate(typeEvaluator, "TTATTTTTTAAAAAAAAAAAAAAA"));
    }

    @Test
    public void testType5() {
        List<TypeEvaluator.Type> type_5 = Collections.singletonList(TypeEvaluator.Type.TYPE_5);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 99, 99, 0, 99, 99, 99);

        assertEquals(type_5, evaluate(typeEvaluator, "AAAAAAAAAATTTTTTTTTTTTTT"));

        typeEvaluator = new TypeEvaluator(sequence, 99, 99, 1, 99, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "AAAAAAAAAATTTTTTTTTTTTTT"));
        assertEquals(type_5, evaluate(typeEvaluator, "AAAAAAAAAAATTTTTTTTTTTTT"));
        assertEquals(type_5, evaluate(typeEvaluator, "AAAAAAAAAATTTTTTATTTTTTT"));
        assertEquals(type_5, evaluate(typeEvaluator, "AAAAAAAAAATTTTTTTTTTTTTA"));
    }

    @Test
    public void testType6() {
        List<TypeEvaluator.Type> type_6 = Collections.singletonList(TypeEvaluator.Type.TYPE_6);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 99, 99, 99, 0, 99, 99);

        assertEquals(type_6, evaluate(typeEvaluator, "AAAAAAAAAATTTTTTTTTTTTTT"));

        typeEvaluator = new TypeEvaluator(sequence, 99, 99, 99, 1, 99, 99);
        assertEquals(discard, evaluate(typeEvaluator, "AAAATTTTTTAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAAATTTTTAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAATATTTTAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAATTATTTAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAATTTATTAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAATTTTATAAAAAAAAAAAAAA"));
        assertEquals(type_6, evaluate(typeEvaluator, "AAAATTTTTAAAAAAAAAAAAAAA"));
    }

    @Test
    public void testType7() {
        List<TypeEvaluator.Type> type_7 = Collections.singletonList(TypeEvaluator.Type.TYPE_7);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 99, 99, 99, 99, 1, 1);

        assertEquals(type_7, evaluate(typeEvaluator, "AAAATTTTTAATTTTTTTTTTTTT"));

        typeEvaluator = new TypeEvaluator(sequence, 99, 99, 99, 99, 1, 1);
        assertEquals(discard, evaluate(typeEvaluator, "AAAATTTTTTAAAAAAAAAAAAAA"));
        assertEquals(type_7, evaluate(typeEvaluator, "AAAAATTTTTATTTTTTTTTTTTT"));
        assertEquals(type_7, evaluate(typeEvaluator, "AAAATTTTTAATTTTTTTTTTTTT"));
    }

    @Test
    public void testAllTypes() {
        List<TypeEvaluator.Type> allTypes = Arrays.asList(TypeEvaluator.Type.TYPE_1, TypeEvaluator.Type.TYPE_2, TypeEvaluator.Type.TYPE_3, TypeEvaluator.Type.TYPE_5, TypeEvaluator.Type.TYPE_6, TypeEvaluator.Type.TYPE_7);
        Sequence sequence = new Sequence("TTTTTTTTTTTTTTTTTTTTTTTT", 0, "test");
        TypeEvaluator typeEvaluator = new TypeEvaluator(sequence, 0, 0, 0, 0, 0, 0);

        assertEquals(allTypes, evaluate(typeEvaluator, "AAAAAAAAAAAAAAAAAAAAAAAA"));

        typeEvaluator = new TypeEvaluator(sequence, 1, 1, 1, 1, 1, 1);
        assertEquals(discard, evaluate(typeEvaluator, "TTTTTTTTTTTTTTTTTTTTTTTT"));
        assertEquals(allTypes, evaluate(typeEvaluator, "ATTTAATTTTATTTTTTTTTTTTT"));
    }

    private List<TypeEvaluator.Type> evaluate(TypeEvaluator typeEvaluator, String sequence) {
        typeEvaluator.evaluate(new Sequence(sequence, 0, "test2"));
        return typeEvaluator.getMatchTypes();
    }
}
