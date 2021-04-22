package com.henrikroslund.sequence;

import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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

    private int pamHash = 0;
    private int seedHash = 0;
    private int n7n20Hash = 0;

    public static final int PAM_INDEX_START = 0;
    public static final int PAM_LENGTH = 4;

    public static final int SEED_LENGTH = 6;
    public static final int SEED_INDEX_START = PAM_INDEX_START + PAM_LENGTH;
    public static final int SEED_INDEX_END = SEED_INDEX_START + SEED_LENGTH - 1;

    public static final int TARGET_LENGTH = 20;
    public static final int TARGET_INDEX_START = SEED_INDEX_START;

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;
    public static final int RAW_INDEX_END = RAW_LENGTH - 1;

    public static final int N7_INDEX = TARGET_INDEX_START + 6;
    public static final int N20_INDEX = TARGET_INDEX_START + 19;

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
    @Setter
    private Map<TypeEvaluator.Type, Integer> metaData;

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome) {
        this(raw, startIndex, genome, false);
    }

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome, boolean isComplement) {
        this(raw, startIndex, genome, isComplement, null);
    }

    @SneakyThrows
    public Sequence(String raw, int startIndex, String genome, boolean isComplement, Map<TypeEvaluator.Type, Integer> metaData) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.startIndex = startIndex;
        this.endIndex = startIndex + RAW_LENGTH - 1;
        this.genome = genome;
        this.isComplement = isComplement;
        this.metaData = metaData;
    }

    public Map<TypeEvaluator.Type, Integer> getMetaData() {
        if(metaData == null) {
            metaData = new HashMap<>();
            for(TypeEvaluator.Type type : TypeEvaluator.Type.values()) {
                metaData.put(type, 0);
            }
        }
        return metaData;
    }

    public void increaseMetaDataCounter(TypeEvaluator.Type key) {
        Integer value = getMetaData().get(key);
        value++;
        metaData.put(key, value);
    }

    public boolean equalsPam(Sequence sequence) {
        return this.getPamHash() == sequence.getPamHash();
    }

    public boolean equalsSeed(Sequence sequence) {
        return this.getSeedHash() == sequence.getSeedHash();
    }

    public boolean equalsN7N20(Sequence sequence) {
        return this.getN7N20Hash() == sequence.getN7N20Hash();
    }

    public boolean getIsComplement() {
        return isComplement;
    }

    public Sequence getComplement() {
        StringBuilder complement = new StringBuilder(RAW_LENGTH);
        for(int i=RAW_LENGTH-1; i>=0; i--) {
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
        if(log.isLoggable(Level.FINE)) {
            log.fine(raw + " " + complement);
        }
        return new Sequence(complement.toString(), startIndex +(RAW_LENGTH-1), genome, true);
    }

    // Equals and hashcode only cares about the raw string
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return getRawHash() == sequence.getRawHash();
    }

    @Override
    public int hashCode() {
        return getRawHash();
    }

    public String metaDataToString() {
        if(metaData == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(120);
        metaData.forEach((s, s2) -> {
            result.append(s).append("=").append(s2).append("&");
        });
        return result.toString();
    }

    public static Map<TypeEvaluator.Type, Integer> stringToMetaData(String metaString) {
        String[] metaDatas = metaString.split("&");
        Map<TypeEvaluator.Type, Integer> result = new HashMap<>();
        for (String data : metaDatas) {
            String[] parts = data.split("=");
            result.put(TypeEvaluator.Type.valueOf(parts[0]), Integer.valueOf(parts[1]));
        }
        return result;
    }

    @Override
    public String toString() {
        return toStringRepresentation(false);
    }

    private String toStringRepresentation(boolean forSerialization) {
        String genomeName = genome;
        if(forSerialization) {
            // This operation is expensive so we only do it during serialization
            genomeName = Utils.getStringWithoutWhitespaces(genome);
        }
        return raw + " " + (isComplement ? "-" : "+") + " " + startIndex + " " + (genomeName != null ? genomeName : "NAME_UNAVAILABLE") + " " + metaDataToString();
    }

    public String serialize() {
        return toStringRepresentation(true);
    }

    public static Sequence parseFromToString(String line) {
        String[] parts = line.split(" ");
        if(parts.length == 4) {
            return new Sequence(parts[0], Integer.parseInt(parts[2]), parts[3], parts[1].compareTo("-") == 0);
        } else if(parts.length == 5) {
            return new Sequence(parts[0], Integer.parseInt(parts[2]), parts[3], parts[1].compareTo("-") == 0, stringToMetaData(parts[4]));
        } else {
            throw new IllegalArgumentException("Unexpected amount of parts " + parts.length + " in line " + line);
        }
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

    /**
     * Will return GC count excluding PAM
     * @return
     */
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

    public int getRawHash() {
        return raw.hashCode();
    }

    public int getPamHash() {
        if(this.pamHash == 0) {
            this.pamHash = raw.substring(PAM_INDEX_START, PAM_LENGTH).hashCode();
        }
        return pamHash;
    }

    public int getSeedHash() {
        if(this.seedHash == 0) {
            this.seedHash = raw.substring(SEED_INDEX_START, SEED_INDEX_END+1).hashCode();
        }
        return seedHash;
    }

    public int getN7N20Hash() {
        if(this.n7n20Hash == 0) {
            this.n7n20Hash = raw.substring(SEED_INDEX_END+1, RAW_INDEX_END).hashCode();
        }
        return n7n20Hash;
    }
}
