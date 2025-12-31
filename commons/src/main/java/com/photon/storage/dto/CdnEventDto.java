package com.photon.storage.dto;

import com.photon.storage.enums.CDNOperation;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CdnEventDto {
    private String metaId;
    private String entityId;
    private CDNOperation operation;
}