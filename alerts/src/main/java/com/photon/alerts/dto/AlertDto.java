package com.photon.alerts.dto;

import com.photon.alerts.entity.Alert;
import com.photon.alerts.enums.AlertAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {

    private Long id;
    private String alertType;
    private String alertSubType;
    private AlertAudience audience;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private List<AlertTemplateDto> templates;

    public static AlertDto from(Alert alert, List<AlertTemplateDto> templates) {
        return AlertDto.builder()
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
}
