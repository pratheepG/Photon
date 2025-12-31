package com.photon.alerts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadedAttachmentDto {
    private String fileName;
    private byte[] fileBytes;
}