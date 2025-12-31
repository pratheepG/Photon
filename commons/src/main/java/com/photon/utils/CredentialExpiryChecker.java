package com.photon.utils;

import java.util.Date;

public class CredentialExpiryChecker {

    /**
     * @param createdDate Date
     * @param credentialExpiryInMinutes long
     * @return boolean
     */
    public static boolean isCredentialExpired(Date createdDate, long credentialExpiryInMinutes) {
        long expiryInMillis = credentialExpiryInMinutes * 60 * 1000L;
        long expirationTime = createdDate.getTime() + expiryInMillis;
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * @param createdDate Date
     * @param credentialExpiryInMinutes double
     * @return boolean
     */
    public static boolean isCredentialExpired(Date createdDate, double credentialExpiryInMinutes) {
        double expiryInMillis = credentialExpiryInMinutes * 60 * 1000L;
        double expirationTime = createdDate.getTime() + expiryInMillis;
        return System.currentTimeMillis() > expirationTime;
    }
}