package com.henrikroslund.sequence;

import com.henrikroslund.Genome;
import com.henrikroslund.exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class represents a sequence.
 * A sequence consists of two parts, PEM and TARGET and is exactly PAM length + TARGET length = 24 characters long.
 * A sequence is valid if both PAM and TARGET is valid.
 * A PAM is valid if there is a match for PAM_MATCH_REGEXP
 * A TARGET is valid if the number of TARGET_MATCH_PATTERN matches are within TARGET_MATCH_MIN and TARGET_MATCH_MAX (inclusive).
 */
@Log
public class CrisprSequence {

    // Unprocessed string
    @Getter
    private final String raw;
    // Index from original string for first character
    private final int index;

    @Getter
    private final String pam;
    private static final int PAM_LENGTH = 4;
    // First three must be Ts and the forth must Not be T
    private static final String PAM_MATCH_REGEXP = "^[T]{3}[^T]";

    @Getter
    private final String target;
    private static final int TARGET_LENGTH = 20;
    private static final int SEED_LENGTH = 6;
    private static final Pattern TARGET_MATCH_GC_CONTENT_PATTERN = Pattern.compile("[GC]");
    private static final int TARGET_MATCH_MIN = 9; // 20*0.45
    private static final int TARGET_MATCH_MAX = 11; // 20*0.55
    private static final Pattern TARGET_MATCH_NO_TRIPLETS_CONTENT_PATTERN = Pattern.compile("[T]{3}|[A]{3}|[G]{3}|[C]{3}");

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;

    private int exactMatchesInOtherGenome = 0;
    @Getter
    private int partialMatchesInOtherGenomes = 0;
    @Getter
    private boolean isPamMismatchInOtherGenomes = true;
    @Getter
    private boolean isSeedMismatchInOtherGenomes = true;
    private int otherGenomeSequencesProcessed = 0;
    private static final int MIN_CONSECUTIVE_MISMATCH_IN_TARGET_WITH_OTHER_GENOME = 1;

    @Setter
    private boolean isComplement = false;

    @Getter
    private final boolean valid;

    @SneakyThrows
    public CrisprSequence(String raw, int index) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.index = index;
        this.pam = raw.substring(0, PAM_LENGTH);
        this.target = raw.substring(PAM_LENGTH);
        if(target.length() != TARGET_LENGTH) {
            throw new InvalidSequenceException("target has length " + target.length() + " but expected " + TARGET_LENGTH);
        }
        valid = isPamMatch() && isTargetMatch();
    }

    public boolean shouldBeFilteredOutBasedOnMatchesInOtherGenomes() {
        return exactMatchesInOtherGenome > 0 || partialMatchesInOtherGenomes > 0;
    }

    public void processMatchesInOtherGenomes(List<Genome> genomes) throws Exception {
        if(otherGenomeSequencesProcessed != 0) {
            throw new Exception("This sequence has already processed a list of genomes. Something is wrong");
        }
        for(Genome genome : genomes) {
            //processSequenceComparison(genome.getValidSequences());
            //processSequenceComparison(genome.getValidComplementSequences());
            ///otherGenomeSequencesProcessed += genome.getValidSequences().size() + genome.getValidComplementSequences().size();
        }
    }

    private void processSequenceComparison(List<CrisprSequence> sequences) {
        for(CrisprSequence sequence : sequences) {
            if(sequence.equals(this)) {
                exactMatchesInOtherGenome++;
            }
            if(isPartialMatch(sequence)) {
                partialMatchesInOtherGenomes++;
            }
        }
    }

    protected boolean isPartialMatch(CrisprSequence sequence) {
        if(isPamDifferent(sequence)) {
            // The PAM is not equal so sequence is not candidate for partial match
            return false;
        }
        isPamMismatchInOtherGenomes = false;

        // The PAM is equal so we check the Target if there are at least two consecutive mismatches
        int maxConsecutiveMismatches = 0;
        int currentConsecutiveMismatches = 0;
        for(int i=0; i<SEED_LENGTH; i++) {
            if(target.charAt(i) == sequence.getTarget().charAt(i)) {
                currentConsecutiveMismatches = 0;
            } else {
                currentConsecutiveMismatches++;
                if(currentConsecutiveMismatches > maxConsecutiveMismatches) {
                    maxConsecutiveMismatches = currentConsecutiveMismatches;
                }
            }
        }
        log.finest("maxConsecutiveMismatches: " + maxConsecutiveMismatches);
        boolean isSeedMatch = maxConsecutiveMismatches < MIN_CONSECUTIVE_MISMATCH_IN_TARGET_WITH_OTHER_GENOME;
        if(isSeedMatch) {
            isSeedMismatchInOtherGenomes = false;
        }
        return isSeedMatch;
    }

    private boolean isPamDifferent(CrisprSequence sequence) {
        return this.pam.compareTo(sequence.getPam()) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrisprSequence sequence = (CrisprSequence) o;
        return raw.compareTo(sequence.getRaw()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    public boolean isPamMatch() {
        return pam.matches(PAM_MATCH_REGEXP);
    }

    public boolean isTargetMatch() {
        long matches = getGCOccurences();
        boolean gcContentValid = matches >= TARGET_MATCH_MIN && matches <= TARGET_MATCH_MAX;
        boolean noTriplets = TARGET_MATCH_NO_TRIPLETS_CONTENT_PATTERN.matcher(target).results().count() == 0;
        return gcContentValid && noTriplets;
    }

    private long getGCOccurences() {
        return TARGET_MATCH_GC_CONTENT_PATTERN.matcher(target).results().count();
    }

    private long getGCPercent() {
        return 100 * getGCOccurences() / TARGET_LENGTH;
    }

    private static double getGCMaxPercent() {
        return 100 * TARGET_MATCH_MAX / TARGET_LENGTH;
    }

    private static double getFCMinPercent() {
        return 100 * TARGET_MATCH_MIN / TARGET_LENGTH;
    }

    public CrisprSequence getComplement() throws Exception {
        String complement = "";
        for(int i=0; i<raw.length(); i++) {
            char character = raw.charAt(i);
            switch (character) {
                case 'A':
                    complement = 'T' + complement;
                    break;
                case 'T':
                    complement = 'A' + complement;
                    break;
                case 'G':
                    complement = 'C' + complement;
                    break;
                case 'C':
                    complement = 'G' + complement;
                    break;
                default:
                    complement = character + complement;
            }
        }
        log.fine(raw + " " + complement);
        CrisprSequence complementSequence = new CrisprSequence(complement, index);
        complementSequence.setComplement(true);
        return complementSequence;
    }

    @Override
    public String toString() {
        return raw + " " + (isComplement ? "-" : "+") + " " + index + " " + getGCPercent() + "%" + " exactMatches:"+exactMatchesInOtherGenome
                + " partialMatches:" + partialMatchesInOtherGenomes + " sequencesComparedWith:"+otherGenomeSequencesProcessed;
    }

    public static String getRulesApplied() {
        String rules = "Target GC Min: " + getFCMinPercent() + "% (" + TARGET_MATCH_MIN + ") Max: "
                + getGCMaxPercent() + "% (" + TARGET_MATCH_MAX + ")\n"
                + "Minimum consecutive mismatch: " + MIN_CONSECUTIVE_MISMATCH_IN_TARGET_WITH_OTHER_GENOME + "\n";
        rules += "*****************************************************************************\n";
        return rules;
    }
}
