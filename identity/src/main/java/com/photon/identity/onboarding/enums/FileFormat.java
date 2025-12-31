package com.photon.identity.onboarding.enums;

import lombok.Getter;

@Getter
public enum FileFormat {
    JPG(FileType.IMAGE),
    PNG(FileType.IMAGE),
    JPEG(FileType.IMAGE),

    MP4(FileType.VIDEO),
    MPEG(FileType.VIDEO),

    EXCEL(FileType.DOCUMENT),
    CSV(FileType.DOCUMENT),
    WORD(FileType.DOCUMENT),
    PDF(FileType.DOCUMENT);

    private final FileType fileType;

    FileFormat(FileType type) {
        this.fileType = type;
    }
}