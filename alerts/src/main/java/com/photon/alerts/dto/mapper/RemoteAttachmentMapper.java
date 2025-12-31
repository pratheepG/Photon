package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.entity.RemoteAttachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteAttachmentMapper {

    public static RemoteAttachmentDto toDto(RemoteAttachment entity) {
        if (entity == null) return null;

        return RemoteAttachmentDto.builder()
                .fileName(entity.getFileName())
                .downloadUrl(entity.getDownloadUrl())
                .build();
    }

    public static RemoteAttachment toEntity(RemoteAttachmentDto dto, Long templateId) {
        if (dto == null) return null;

        return RemoteAttachment.builder()
                .templateId(templateId)
                .fileName(dto.getFileName())
                .downloadUrl(dto.getDownloadUrl())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static List<RemoteAttachmentDto> toDtoList(List<RemoteAttachment> entities) {
        return entities.stream().map(RemoteAttachmentMapper::toDto).collect(Collectors.toList());
    }

    public static List<RemoteAttachment> toEntityList(List<RemoteAttachmentDto> dtos, Long templateId) {
        return dtos.stream()
                .map(dto -> toEntity(dto, templateId))
                .collect(Collectors.toList());
    }
}