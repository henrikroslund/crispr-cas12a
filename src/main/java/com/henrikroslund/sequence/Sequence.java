package com.henrikroslund.sequence;

import com.henrikroslund.exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

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
    protected final int rawHash;
    protected final int pamHash;
    protected final int seedHash;

    public static final int PAM_INDEX_START = 0;
    public static final int PAM_LENGTH = 4;

    public static final int SEED_LENGTH = 6;
    public static final int SEED_INDEX_START = PAM_INDEX_START + PAM_LENGTH;
    public static final int SEED_INDEX_END = SEED_INDEX_START + SEED_LENGTH;

    public static final int TARGET_LENGTH = 20;

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;

    // Will only be initialized on first get
    private Integer gcCount = null;

    // Index of start of sequence. The index is always based on the positive strand.
    @Getter
    private final int startIndex;
    @Getter
    private final int endIndex;

    private final boolean isComplement;

    private final String genome;

    // metaData is created upon first get to save memory
    private Map<String, String> metaData = null;

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome) {
        this(raw, startIndex, genome, false);
    }

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome, boolean isComplement) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.rawHash = raw.hashCode();
        this.pamHash = raw.substring(PAM_INDEX_START, PAM_LENGTH).hashCode();
        this.seedHash = raw.substring(SEED_INDEX_START, SEED_INDEX_END).hashCode();
        this.startIndex = startIndex;
        this.endIndex = startIndex + RAW_LENGTH - 1;
        this.genome = genome.replaceAll("\\s","");
        this.isComplement = isComplement;
    }

    public Map<String, String> getMetaData() {
        if(metaData == null) {
            metaData = new HashMap<>();
        }
        return metaData;
    }

    public boolean equalsPam(Sequence sequence) {
        return this.pamHash == sequence.pamHash;
    }

    public boolean equalsSeed(Sequence sequence) {
        return this.seedHash == sequence.seedHash;
    }

    public boolean getIsComplement() {
        return isComplement;
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
        Sequence complementSequence = new Sequence(complement.reverse().toString(), startIndex +(raw.length()-1), genome, true);
        return complementSequence;
    }

    // Equals and hashcode only cares about the raw string
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return rawHash == sequence.rawHash;
    }

    @Override
    public int hashCode() {
        return rawHash;
    }

    @Override
    public String toString() {
        return raw + " " + (isComplement ? "-" : "+") + " " + startIndex + (genome != null ? " " + genome : "");
    }

    public static Sequence parseFromToString(String line) {
        String[] parts = line.split(" ");
        if(parts.length != 4) {
            throw new IllegalArgumentException("Unexpected amount of parts " + parts.length + " in line " + line);
        }
        return new Sequence(parts[0], Integer.parseInt(parts[2]), parts[3], parts[1].compareTo("-") == 0);
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

    public int getGCCount() {
        if(gcCount == null) {
            int count = 0;
            for(int i=Sequence.PAM_LENGTH; i<RAW_LENGTH; i++) {
                char currentChar = raw.charAt(i);
                if(currentChar == 'G' || currentChar == 'C') {
                    count++;
                }
            }
            gcCount = count;
        }
        return gcCount;
    }
}
