package com.photon.enums;

public enum SecurityLevel {
    PUBLIC("Publicly available for all users"),
    AUTHENTICATED("Accessible only for authenticated users ( authenticated by any of the AuthType )"),
    ANONYMOUS("Accessible for the user with CLIENT_ID and CLIENT_SECRET"),
    PRIVATE("Accessible for internal server");

    public final String description;

    SecurityLevel(String description){
        this.description = description;
    }
}