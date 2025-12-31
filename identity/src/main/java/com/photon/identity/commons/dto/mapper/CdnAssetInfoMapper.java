package com.photon.identity.commons.dto.mapper;

import com.photon.identity.commons.dto.request.CdnAssetInfoDto;
import com.photon.identity.commons.entity.CdnAssetInfo;

public class CdnAssetInfoMapper {

    public static CdnAssetInfoDto toDto(CdnAssetInfo entity){
        CdnAssetInfoDto dto = new CdnAssetInfoDto();
        dto.setId(entity.getId());
        dto.setUrl(entity.getUrl());
        dto.setMetaId(entity.getMetaId());
        return dto;
    }

    public static CdnAssetInfo toEntity(CdnAssetInfoDto dto){
        CdnAssetInfo entity = new CdnAssetInfo();
        entity.setUrl(dto.getUrl());
        entity.setMetaId(dto.getMetaId());
        return entity;
    }
}