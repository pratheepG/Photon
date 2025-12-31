package com.photon.alerts.dto.response;

import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.entity.AlertTemplate;
import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertTemplateResponseDto {
    private Long id;
    private String templateId;
    private Long alertId;
    private Channel channel;
    private String subjectTemplate;
    private String messageTemplate;
    private String smsDeeplinkUrl;
    private String smsMediaUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private List<RemoteAttachmentDto> mailAttachments;

    public static AlertTemplateResponseDto toDto(AlertTemplate template) {
        return AlertTemplateResponseDto.builder()
                .id(template.getId())
                .alertId(template.getAlertId())
                .channel(template.getChannel())
                .subjectTemplate(template.getSubjectTemplate())
                .messageTemplate(template.getMessageTemplate())
                .smsDeeplinkUrl(template.getSmsDeeplinkUrl())
                .smsMediaUrl(template.getSmsMediaUrl())
                .templateId(template.getTemplateId())
                .createdAt(template.getCreatedAt())
                .modifiedAt(template.getModifiedAt())
                .build();
    }
}