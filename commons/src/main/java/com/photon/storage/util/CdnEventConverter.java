package com.photon.storage.util;

import com.photon.storage.dto.CdnEventDto;
import com.photon.storage.proto.CdnEvent;

public class CdnEventConverter {
    public static CdnEvent toProtoBuf(CdnEventDto dto){
        return CdnEvent.newBuilder()
                .setMetaId(dto.getMetaId())
                .setEntityId(dto.getEntityId())
                .build();
    }

    public static CdnEventDto toDto(CdnEvent proto){
        return CdnEventDto.builder()
                .metaId(proto.getMetaId())
                .entityId(proto.getEntityId())
                .build();
    }
}