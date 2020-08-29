import exceptions.InvalidSequenceException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a sequence
 */
@Log
public class Sequence {

    // Unprocessed string
    @Getter
    private final String raw;
    // Index from original string for first character
    private final int index;

    private final String seed;
    private static final int SEED_LENGTH = 4;
    // First three must be Ts and the forth must Not be T
    private static final String SEED_MATCH_REGEXP = "^[T]{3}[^T]";

    private final String pam;
    private static final int PAM_LENGTH = 20;
    private static final Pattern PAM_MATCH_PATTERN = Pattern.compile("[GC]");
    private static final int PAM_MATCH_MIN = 9; // 20*0.45
    private static final int PAM_MATCH_MAX = 11; // 20*0.45

    public static final int RAW_LENGTH = SEED_LENGTH + PAM_LENGTH;

    @Getter
    private final boolean valid;

    @SneakyThrows
    public Sequence(String raw, int index) {
        if(raw.length() != RAW_LENGTH) {
            throw new InvalidSequenceException("Raw sequence has length " + raw.length() + " but expected " + RAW_LENGTH);
        }
        this.raw = raw;
        this.index = index;
        this.seed = raw.substring(0, SEED_LENGTH);
        this.pam = raw.substring(SEED_LENGTH);
        if(pam.length() != PAM_LENGTH) {
            throw new InvalidSequenceException("PAM has length " + pam.length() + " but expected " + PAM_LENGTH);
        }
        valid = isSeedMatch() && isPamMatch();
    }

    public boolean isSeedMatch() {
        return seed.matches(SEED_MATCH_REGEXP);
    }

    public boolean isPamMatch() {
        Matcher matcher = PAM_MATCH_PATTERN.matcher(pam);
        long matches = matcher.results().count();
        return matches >= PAM_MATCH_MIN && matches <= PAM_MATCH_MAX;
    }

    @Override
    public String toString() {
        return raw;
    }
}
