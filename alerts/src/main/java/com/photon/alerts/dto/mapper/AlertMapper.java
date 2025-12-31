package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.request.AlertRequestDto;
import com.photon.alerts.dto.response.AlertResponseDto;
import com.photon.alerts.dto.response.AlertTemplateResponseDto;
import com.photon.alerts.entity.Alert;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class AlertMapper {

    public static Alert toEntity(AlertRequestDto dto) {
        return Alert.builder()
                .alertType(dto.getAlertType())
                .alertSubType(dto.getAlertSubType())
                .audience(dto.getAudience())
                .topic(dto.getTopic())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static AlertResponseDto toDto(Alert alert, List<AlertTemplateResponseDto> templates) {
        return AlertResponseDto.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .alertSubType(alert.getAlertSubType())
                .audience(alert.getAudience())
                .topic(alert.getTopic())
                .createdAt(alert.getCreatedAt())
                .modifiedAt(alert.getModifiedAt())
                .templates(templates)
                .build();
    }

    public static AlertResponseDto toDto(Alert alert) {
        return toDto(alert, Collections.emptyList());
    }
}