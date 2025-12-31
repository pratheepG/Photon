package com.photon.storage.enums;

import lombok.Getter;

@Getter
public enum ImagePreset {
    THUMBNAIL(150, 150, 0.5f),    // 50% quality
    LIST_VIEW(300, 300, 0.7f),    // 70% quality
    FULL_SIZE(1000, 1000, 0.9f);  // 90% quality

    private final int width;
    private final int height;
    private final float quality;

    ImagePreset(int width, int height, float quality) {
        this.width = width;
        this.height = height;
        this.quality = quality;
    }

}