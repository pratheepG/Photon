package com.photon.endpoint.enums;

public enum AccessLevel {
    NONE("It provide none of the resource access"),
    VIEWER("It provide read-only access for the resources"),
    EDITOR("It provide read and edit access for the resources"),
    OWNER("It provide read and edit access for the owner's resources"),
    ADMIN("It provide global access for all resources"),
    TENANT_ADMIN("It provide tenant-level access for all resources");

    public final String description;

    AccessLevel(String description){
        this.description = description;
    }
}