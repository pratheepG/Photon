package com.photon.console.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
public class SnapshotUploadDto {
    private MultipartFile jar;
    private String dockerfile;
    private Map<String, String> env;
    private String serviceName;
}