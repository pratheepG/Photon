package com.photon.alerts.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.alerts.entity.Alert;
import com.photon.alerts.entity.AlertTemplate;
import com.photon.alerts.enums.AlertAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertResponseDto {
    private Long id;
    private String alertType;
    private String alertSubType;
    private AlertAudience audience;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<AlertTemplateResponseDto> templates;

    public static AlertResponseDto toDto(Alert alert, List<AlertTemplate> templates) {
        return AlertResponseDto.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .alertSubType(alert.getAlertSubType())
                .audience(alert.getAudience())
                .topic(alert.getTopic())
                .createdAt(alert.getCreatedAt())
                .modifiedAt(alert.getModifiedAt())
                .templates(templates.stream().map(AlertTemplateResponseDto::toDto).toList())
                .build();
    }

    public static AlertResponseDto toDto(Alert alert) {
        return AlertResponseDto.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .alertSubType(alert.getAlertSubType())
                .audience(alert.getAudience())
                .topic(alert.getTopic())
                .createdAt(alert.getCreatedAt())
                .modifiedAt(alert.getModifiedAt())
                .templates(Collections.emptyList())
                .build();
    }
}