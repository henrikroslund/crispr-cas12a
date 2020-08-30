import exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a sequence.
 * A sequence consists of two parts, PEM and TARGET and is exactly PAM length + TARGET length = 24 characters long.
 * A sequence is valid if both PAM and TARGET is valid.
 * A PAM is valid if there is a match for PAM_MATCH_REGEXP
 * A TARGET is valid if the number of TARGET_MATCH_PATTERN matches are within TARGET_MATCH_MIN and TARGET_MATCH_MAX (inclusive).
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

    private final String target;
    private static final int TARGET_LENGTH = 20;
    private static final Pattern TARGET_MATCH_PATTERN = Pattern.compile("[GC]"); // TODO VERIFY WITH POP
    private static final int TARGET_MATCH_MIN = 9; // 20*0.45
    private static final int TARGET_MATCH_MAX = 11; // 20*0.45

    public static final int RAW_LENGTH = PAM_LENGTH + TARGET_LENGTH;

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
        this.target = raw.substring(PAM_LENGTH);
        if(target.length() != TARGET_LENGTH) {
            throw new InvalidSequenceException("target has length " + target.length() + " but expected " + TARGET_LENGTH);
        }
        valid = isPamMatch() && isTargetMatch();
    }

    public boolean isPamMatch() {
        return pam.matches(PAM_MATCH_REGEXP);
    }

    public boolean isTargetMatch() {
        Matcher matcher = TARGET_MATCH_PATTERN.matcher(target);
        long matches = matcher.results().count();
        return matches >= TARGET_MATCH_MIN && matches <= TARGET_MATCH_MAX;
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
