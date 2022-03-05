package com.henrikroslund;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.henrikroslund.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testIsChromosome() {
        assertTrue(isChromosomeFile("bla " + CHROMOSOME_STRING +" 1.fasta"));
        assertTrue(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2.fasta"));

        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2A.fasta"));
        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +" 2 .fasta"));
        assertFalse(isChromosomeFile("bla " + CHROMOSOME_STRING +"A 2.fasta"));
        assertFalse(isChromosomeFile("something 1"));
    }

    @Test
    public void testIsPrimaryChromosomeFile() {
        assertTrue(isPrimaryChromosomeFile("bla " + CHROMOSOME_STRING +" 1.fasta"));
        assertFalse(isPrimaryChromosomeFile("bla " + CHROMOSOME_STRING +" 2.fasta"));
    }

    @Test
    public void testGetChromosomeFiles() {
        ArrayList<String> files = getChromosomeFiles("bla " + CHROMOSOME_STRING +" 1.fasta");
        assertEquals(5, files.size());
        assertEquals("bla " + CHROMOSOME_STRING +" 1.fasta", files.get(0));
        assertEquals("bla " + CHROMOSOME_STRING +" 2.fasta", files.get(1));
        assertEquals("bla " + CHROMOSOME_STRING +" 3.fasta", files.get(2));
        assertEquals("bla " + CHROMOSOME_STRING +" 4.fasta", files.get(3));
        assertEquals("bla " + CHROMOSOME_STRING +" 5.fasta", files.get(4));
    }
}