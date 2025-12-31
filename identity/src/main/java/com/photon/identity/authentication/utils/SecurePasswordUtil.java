package com.photon.identity.authentication.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public final class SecurePasswordUtil {
    private static final SecureRandom RNG = new SecureRandom();

    private static final char[] UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWER = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGIT = "0123456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?".toCharArray();
    private static final char[] ALL;

    static {
        String all = new String(UPPER) + new String(LOWER) + new String(DIGIT) + new String(SPECIAL);
        ALL = all.toCharArray();
    }

    private SecurePasswordUtil() {}

    /** Generates a password as char[] so it can be wiped after use. */
    public static char[] generatePassword(int length, boolean requireAllClasses) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");

        if (requireAllClasses && length < 4) {
            throw new IllegalArgumentException("length must be >= 4 when requireAllClasses=true");
        }

        char[] out = new char[length];
        int idx = 0;

        if (requireAllClasses) {
            out[idx++] = randomFrom(UPPER);
            out[idx++] = randomFrom(LOWER);
            out[idx++] = randomFrom(DIGIT);
            out[idx++] = randomFrom(SPECIAL);
        }

        while (idx < length) {
            out[idx++] = randomFrom(ALL);
        }

        for (int i = out.length - 1; i > 0; i--) {
            int j = RNG.nextInt(i + 1);
            char tmp = out[i];
            out[i] = out[j];
            out[j] = tmp;
        }

        return out;
    }

    /** Overwrite the char[] in-place. Call in finally blocks. */
    public static void wipe(char[] secret) {
        if (secret != null) Arrays.fill(secret, '\0');
    }

    /** Example: derive a PBKDF2 hash from char[] without creating a String. */
    public static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static char randomFrom(char[] alphabet) {
        return alphabet[RNG.nextInt(alphabet.length)];
    }
}