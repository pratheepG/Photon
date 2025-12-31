package com.photon.storage.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private UUID metaId;
    private String fileName;
    private String fileUrl;
    private String fileFormat;
    private long fileSize;
    private String preset;
}