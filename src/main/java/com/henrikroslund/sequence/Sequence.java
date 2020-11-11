package com.henrikroslund.sequence;

import com.henrikroslund.exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.Objects;

/**
 * This class represents a sequence.
 * A sequence consists of two parts, PEM and TARGET and is exactly PAM length + TARGET length = 24 characters long.
 * A sequence is valid if both PAM and TARGET is valid.
 * A PAM is valid if there is a match for PAM_MATCH_REGEXP
 * A TARGET is valid if the number of TARGET_MATCH_PATTERN matches are within TARGET_MATCH_MIN and TARGET_MATCH_MAX (inclusive).
 */
@Log
public class Sequence implements Comparable<Sequence> {

    @Getter
    private final String raw;
    // Index of start of sequence. The index is always based on the positive strand.
    @Getter
    private final int startIndex;
    @Getter
    private final int endIndex;

    private static final int PAM_INDEX_START = 0;
    private static final int PAM_LENGTH = 4;

    // First three must be Ts and the forth must Not be T for crispr
    private static final String CRISPR_PAM_MATCH_REGEXP = "^[T]{3}[^T]";

    @Getter
    private static final int TARGET_LENGTH = 20;

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;

    @Setter
    private boolean isComplement = false;

    @Setter
    private String genome;

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.startIndex = startIndex;
        this.endIndex = startIndex + RAW_LENGTH - 1;
        this.genome = genome;
    }

    public boolean getIsComplement() {
        return isComplement;
    }

    private boolean isPamDifferent(Sequence sequence) {
        return raw.regionMatches(PAM_INDEX_START, sequence.getRaw(), PAM_INDEX_START, PAM_LENGTH);
    }

    public Sequence getComplement() {
        StringBuilder complement = new StringBuilder(raw.length());
        for(int i=0; i<raw.length(); i++) {
            char character = raw.charAt(i);
            switch (character) {
                case 'A':
                    complement.append('T');
                    break;
                case 'T':
                    complement.append('A');
                    break;
                case 'G':
                    complement.append('C');
                    break;
                case 'C':
                    complement.append('G');
                    break;
                default:
                    complement.append(character);
            }
        }
        log.fine(raw + " " + complement);
        Sequence complementSequence = new Sequence(complement.reverse().toString(), startIndex +(raw.length()-1), genome);
        complementSequence.setComplement(true);
        return complementSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return raw.compareTo(sequence.getRaw()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public String toString() {
        return raw + " " + (isComplement ? "-" : "+") + " " + startIndex + (genome != null ? " " + genome : "");
    }

    /**
     * Compares the start index of the sequences.
     * A lower index number is considered to be greater.
     * Complement is always considered to be greater.
     */
    @Override
    public int compareTo(Sequence o) {
        int genomeCompare = this.genome.compareTo(o.genome);
        if(genomeCompare != 0) {
            return genomeCompare;
        }
        if(this.isComplement != o.isComplement) {
            return this.isComplement ? 1 : -1;
        }
        if(this.startIndex == o.startIndex) {
            return 0;
        }
        return this.startIndex < o.startIndex ? -1 : 1;
    }
}
