package com.photon.enums;

public enum AssetType {

    IMAGE("image/jpeg"),
    VIDEO("video");

    private final String string;

    public String getValue() {
        return this.string;
    }

    AssetType(String string) {
        this.string = string;
    }

    public static AssetType fromString(String value) {
        for (AssetType assetType : AssetType.values()) {
            if (assetType.getValue().equalsIgnoreCase(value)) {
                return assetType;
            }
        }
        throw new IllegalArgumentException("Invalid AssetType: " + value);
    }

}