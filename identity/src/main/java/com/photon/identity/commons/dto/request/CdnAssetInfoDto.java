package com.photon.identity.commons.dto.request;

import lombok.*;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdnAssetInfoDto {
    private Long id;
    private String metaId;
    private String url;
}