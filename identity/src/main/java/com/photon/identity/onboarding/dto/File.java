package com.photon.identity.onboarding.dto;

import com.photon.identity.onboarding.enums.FileFormat;
import com.photon.identity.onboarding.enums.FileType;
import com.photon.identity.onboarding.enums.SizeUnit;
import com.photon.identity.onboarding.utils.ValidFileFormatsForType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
@ValidFileFormatsForType
public class File {
    private FileType type;
    private SizeUnit unit;
    private long minFileSize;
    private long maxFileSize;
    private Set<FileFormat> supportedFormats;
}