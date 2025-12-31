package com.photon.identity.onboarding.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
public class FileField implements FieldConfig {
    private Set<File> files;
    private int minNumberOfFiles;
    private int maxNumberOfFiles;
}