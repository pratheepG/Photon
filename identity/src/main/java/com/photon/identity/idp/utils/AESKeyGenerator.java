package com.photon.identity.idp.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class AESKeyGenerator {
    public static String generate(int byteSize) {
        byte[] key = new byte[byteSize];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}