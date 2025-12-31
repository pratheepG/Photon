package com.photon.identity.commons.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum AuthAdaptor {
    INSTAGRAM_O_AUTH_2("INSTAGRAM_O_AUTH_2", "FIRST_FACTOR"),
    FACEBOOK_O_AUTH_2("FACEBOOK_O_AUTH_2", "FIRST_FACTOR"),
    GOOGLE_O_AUTH_2("GOOGLE_O_AUTH_2", "FIRST_FACTOR"),
    TWITTER_O_AUTH_2("TWITTER_O_AUTH_2", "FIRST_FACTOR"),
    OKTA_O_AUTH_2("OKTA_O_AUTH_2", "FIRST_FACTOR"),
    O_AUTH_2("O_AUTH_2", "FIRST_FACTOR"),
    STATIC_PWD("STATIC_PWD", "FIRST_FACTOR"),
    SMS_OTP("SMS_OTP", "BOTH"),
    EML_OTP("EML_OTP", "BOTH"),
    ACTIVATION_CODE("ACTIVATION_CODE", "SECOND_FACTOR"),
    TMP_PWD("TMP_PWD", "FIRST_FACTOR"),
    PUSH_APPROVE("PUSH_APPROVE", "SECOND_FACTOR");

    private final String name;
    private final String factor;

    AuthAdaptor(String name, String factor) {
        this.name = name;
        this.factor = factor;
    }

    public static Map<String, String> toMap() {
        return Arrays.stream(AuthAdaptor.values())
                .collect(Collectors.toMap(AuthAdaptor::getName, AuthAdaptor::getFactor));
    }
}