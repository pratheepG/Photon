package com.photon.identity.onboarding.enums;

import lombok.Getter;

@Getter
public enum FileType {
    IMAGE("Image"),
    VIDEO("Video"),
    DOCUMENT("Document");

    private final String value;

    FileType(String s){
        this.value = s;
    }
}