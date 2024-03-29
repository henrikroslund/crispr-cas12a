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

import com.henrikroslund.Utils;
import com.henrikroslund.evaluators.comparisons.TypeEvaluator;
import com.henrikroslund.exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class represents a sequence.
 * A sequence consists of two parts, PAM and TARGET and is exactly PAM length + TARGET length = 24 characters long.
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
    private int i10t17Hash = 0;
    private int i18t23Hash = 0;

    public static final int PAM_INDEX_START = 0;
    public static final int PAM_LENGTH = 4;

    public static final int SEED_LENGTH = 6;
    public static final int SEED_INDEX_START = PAM_INDEX_START + PAM_LENGTH;
    public static final int SEED_INDEX_END = SEED_INDEX_START + SEED_LENGTH - 1;

    public static final int TARGET_LENGTH = 20;
    public static final int TARGET_INDEX_START = SEED_INDEX_START;

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;
    public static final int RAW_INDEX_END = RAW_LENGTH - 1;

    public static final int N1_INDEX = TARGET_INDEX_START;
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

    @Getter
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

    // This method is synchronized because it is not thread safe
    public synchronized void increaseMetaDataCounters(List<TypeEvaluator.Type> keys) {
        keys.forEach(key -> {
            Integer value = getMetaData().get(key);
            value++;
            metaData.put(key, value);
        });
    }

    public boolean equalsPam(Sequence sequence) {
        return this.getPamHash() == sequence.getPamHash();
    }

    public boolean equalsSeed(Sequence sequence) {
        return this.getSeedHash() == sequence.getSeedHash();
    }

    public boolean getIsComplement() {
        return isComplement;
    }

    public String getStrandRepresentation() {
        return isComplement ? "-" : "+";
    }

    /**
     * Will return complement of input sequence. For example:
     * Input sequence:  ATGC
     * Output Sequence: GCAT
     */
    public static String getComplement(String sequence) {
        StringBuilder complement = new StringBuilder(sequence.length());
        for(int i=sequence.length()-1; i>=0; i--) {
            char character = sequence.charAt(i);
            switch (character) {
                case 'A' -> complement.append('T');
                case 'T' -> complement.append('A');
                case 'G' -> complement.append('C');
                case 'C' -> complement.append('G');
                default -> complement.append(character);
            }
        }
        if(log.isLoggable(Level.FINE)) {
            log.fine(sequence + " " + complement);
        }
        return complement.toString();
    }

    public Sequence getComplement() {
        return new Sequence(getComplement(raw), startIndex +(RAW_LENGTH-1), genome, true);
    }

    public String metaDataToString() {
        if(metaData == null) {
            return "";
        }
        if(metaData.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(120);
        metaData.forEach((s, s2) -> result.append(s).append("=").append(s2).append("&"));
        result.deleteCharAt(result.length()-1);
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
     * Since we want two sequences to be considered equal when we add two
     * sequences to a treeset with same raw string - then we must only
     * compare the hashes here
     */
    @Override
    public int compareTo(Sequence o) {
        if(getPamHash() > o.getPamHash()) {
            return 1;
        } else if(getPamHash() < o.getPamHash()) {
            return -1;
        }

        if(getSeedHash() > o.getSeedHash()) {
            return 1;
        } else if(getSeedHash() < o.getSeedHash()) {
            return -1;
        }

        if(getI10t17Hash() > o.getI10t17Hash()) {
            return 1;
        } else if(getI10t17Hash() < o.getI10t17Hash()) {
            return -1;
        }

        if(getI18t23Hash() > o.getI18t23Hash()) {
            return 1;
        } else if(getI18t23Hash() < o.getI18t23Hash()) {
            return -1;
        }

        return 0;
    }

    /**
     * Will return GC count excluding PAM
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

    // The raw hash should never be used as it has large amounts of hash collisions
    private int getRawHash() {
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

    private int getI10t17Hash() {
        if(this.i10t17Hash == 0) {
            this.i10t17Hash = raw.substring(10, 17+1).hashCode();
        }
        return this.i10t17Hash;
    }

    private int getI18t23Hash() {
        if(this.i18t23Hash == 0) {
            this.i18t23Hash = raw.substring(18).hashCode();
        }
        return i18t23Hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        // Because of so many hash collisions with the raw string we instead split that into subsection we know
        // do not have any collisions and then we check all of those.
        // We do this instead of comparing the characters one by one as that would have a higher performance
        // impact when running huge amount of equals.
        return getRawHash() == sequence.getRawHash() && getPamHash() == sequence.getPamHash() &&
                getI10t17Hash() == sequence.getI10t17Hash() && getI18t23Hash() == sequence.getI18t23Hash();
    }

    @Override
    public int hashCode() {
        log.severe("hashCode must never be used in sequence as it is not reliable");
        System.exit(1);
        return 0;
    }
}
