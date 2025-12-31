package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.request.AlertTemplateRequestDto;
import com.photon.alerts.dto.response.AlertTemplateResponseDto;
import com.photon.alerts.entity.Alert;
import com.photon.alerts.entity.AlertTemplate;
import com.photon.alerts.entity.RemoteAttachment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertTemplateMapper {

    public static AlertTemplate toEntity(AlertTemplateRequestDto dto, String templateId, Long alertId) {
        return AlertTemplate.builder()
                .templateId(templateId)
                .alertId(alertId)
                .channel(dto.getChannel())
                .subjectTemplate(dto.getSubjectTemplate())
                .messageTemplate(dto.getMessageTemplate())
                .smsDeeplinkUrl(dto.getSmsDeeplinkUrl())
                .smsMediaUrl(dto.getSmsMediaUrl())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static AlertTemplateResponseDto toDto(AlertTemplate template, List<RemoteAttachment> attachments) {
        return AlertTemplateResponseDto.builder()
                .id(template.getId())
                .templateId(template.getTemplateId())
                .alertId(template.getAlertId())
                .channel(template.getChannel())
                .subjectTemplate(template.getSubjectTemplate())
                .messageTemplate(template.getMessageTemplate())
                .smsDeeplinkUrl(template.getSmsDeeplinkUrl())
                .smsMediaUrl(template.getSmsMediaUrl())
                .createdAt(template.getCreatedAt())
                .modifiedAt(template.getModifiedAt())
                .mailAttachments(RemoteAttachmentMapper.toDtoList(attachments))
                .build();
    }

    public static List<AlertTemplate> toEntityList(List<AlertTemplateRequestDto> dtos, Alert alert) {
        List<AlertTemplate> list = new ArrayList<>();
        for (AlertTemplateRequestDto dto : dtos) {
            String templateId = alert.getAlertType().concat("_")
                            .concat(alert.getAlertSubType()).concat("_")
                            .concat(dto.getChannel().name());
            list.add(toEntity(dto, templateId, alert.getId()));
        }
        return list;
    }

    public static AlertTemplate toEntity(AlertTemplateRequestDto dto, Long alertId) {
        return AlertTemplate.builder()
                .alertId(alertId)
                .channel(dto.getChannel())
                .subjectTemplate(dto.getSubjectTemplate())
                .messageTemplate(dto.getMessageTemplate())
                .smsDeeplinkUrl(dto.getSmsDeeplinkUrl())
                .smsMediaUrl(dto.getSmsMediaUrl())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

}