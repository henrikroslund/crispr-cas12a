import exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a sequence.
 * A sequence consists of two parts, PEM and SEED and is exactly PAM length + SEED length = 24 characters long.
 * A sequence is valid if both PAM and SEED is valid.
 * A PAM is valid if there is a match for PAM_MATCH_REGEXP
 * A SEED is valid if the number of SEED_MATCH_PATTERN matches are within SEED_MATCH_MIN and SEED_MATCH_MAX (inclusive).
 */
@Log
public class Sequence {

    // Unprocessed string
    @Getter
    private final String raw;
    // Index from original string for first character
    private final int index;

    private final String pam;
    private static final int PAM_LENGTH = 4;
    // First three must be Ts and the forth must Not be T
    private static final String PAM_MATCH_REGEXP = "^[T]{3}[^T]";

    private final String seed;
    private static final int SEED_LENGTH = 20; // TODO check with pop is SEED is all 20 or just the 6. IF so, what are the rest called....
    private static final Pattern SEED_MATCH_PATTERN = Pattern.compile("[GC]"); // TODO VERIFY WITH POP
    private static final int SEED_MATCH_MIN = 9; // 20*0.45
    private static final int SEED_MATCH_MAX = 11; // 20*0.45

    public static final int RAW_LENGTH = PAM_LENGTH + SEED_LENGTH;

    @Getter
    private final boolean valid;

    @SneakyThrows
    public Sequence(String raw, int index) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.index = index;
        this.pam = raw.substring(0, PAM_LENGTH);
        this.seed = raw.substring(PAM_LENGTH);
        if(seed.length() != SEED_LENGTH) {
            throw new InvalidSequenceException("SEED has length " + seed.length() + " but expected " + SEED_LENGTH);
        }
        valid = isPamMatch() && isSeedMatch();
    }

    public boolean isPamMatch() {
        return pam.matches(PAM_MATCH_REGEXP);
    }

    public boolean isSeedMatch() {
        Matcher matcher = SEED_MATCH_PATTERN.matcher(seed);
        long matches = matcher.results().count();
        return matches >= SEED_MATCH_MIN && matches <= SEED_MATCH_MAX;
    }

    public Sequence getComplement() throws Exception {
        String complement = "";
        for(int i=0; i<raw.length(); i++) {
            char character = raw.charAt(i);
            switch (character) {
                case 'A':
                    complement += 'T';
                    break;
                case 'T':
                    complement += 'A';
                    break;
                case 'G':
                    complement += 'C';
                    break;
                case 'C':
                    complement += 'G';
                    break;
                default:
                    complement += character;
            }
        }
        log.fine(raw + " " + complement);
        return new Sequence(complement, index);
    }

    @Override
    public String toString() {
        return raw;
    }
}
