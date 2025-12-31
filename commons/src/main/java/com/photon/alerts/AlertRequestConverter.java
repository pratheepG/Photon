package com.photon.alerts;

import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.proto.AlertRequest;
import com.photon.alerts.proto.Channel;
import com.photon.alerts.proto.RemoteAttachment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class AlertRequestConverter {

    public static AlertRequest convertToProtobuf(AlertEventDto dto) {
        AlertRequest.Builder builder = AlertRequest.newBuilder()
                .setAlertType(dto.getAlertType())
                .setAlertSubType(dto.getAlertSubType())
                .setStatus(dto.getStatus())
                .putAllPlaceholders(dto.getPlaceholders())
                .setIsPassword(dto.isPassword())
                .setSmsDeeplinkUrl(Objects.requireNonNullElse(dto.getSmsDeeplinkUrl(), ""))
                .setSmsMediaUrl(Objects.requireNonNullElse(dto.getSmsMediaUrl(),""))
                .setIsPreLogin(dto.isPreLogin())
                .setUserId(Objects.requireNonNullElse(dto.getUserId(),""))
                .setTopic(Objects.requireNonNullElse(dto.getTopic(),""))
                .setPhoneNumber(Objects.requireNonNullElse(dto.getPhoneNumber(),""))
                .setCountryCode(Objects.requireNonNullElse(dto.getCountryCode(),""))
                .setEmail(Objects.requireNonNullElse(dto.getEmail(),""))
                .setChannel(Objects.isNull(dto.getChannel())? Channel.ALL : com.photon.alerts.proto.Channel.valueOf(dto.getChannel().name()))
                .setAlertAudience(com.photon.alerts.proto.AlertAudience.valueOf(dto.getAlertAudience().name()));

        if(Objects.nonNull(dto.getAttachments()) && !dto.getAttachments().isEmpty()) {
            dto.getAttachments().stream()
                    .map(AlertRequestConverter::convertToRemoteAttachment)
                    .forEach(builder::addRemoteAttachment);
        }

        return builder.build();
    }

    public static RemoteAttachment convertToRemoteAttachment(RemoteAttachmentDto dto) {
        return RemoteAttachment.newBuilder()
                .setFileName(dto.getFileName())
                .setDownloadUrl(dto.getDownloadUrl())
                .build();
    }

    public static AlertEventDto convertFromProtobuf(AlertRequest proto) {
        AlertEventDto dto = new AlertEventDto();
        dto.setAlertType(proto.getAlertType());
        dto.setAlertSubType(proto.getAlertSubType());
        dto.setStatus(proto.getStatus());
        dto.setPlaceholders(proto.getPlaceholdersMap());
        dto.setPassword(proto.getIsPassword());
        dto.setSmsDeeplinkUrl(proto.getSmsDeeplinkUrl());
        dto.setSmsMediaUrl(proto.getSmsMediaUrl());
        dto.setUserId(proto.getUserId());
        dto.setTopic(proto.getTopic());
        dto.setPreLogin(proto.getIsPreLogin());
        dto.setPhoneNumber(proto.getPhoneNumber());
        dto.setCountryCode(proto.getCountryCode());
        dto.setEmail(proto.getEmail());
        dto.setChannel(com.photon.alerts.enums.Channel.valueOf(proto.getChannel().name()));
        dto.setAlertAudience(com.photon.alerts.enums.AlertAudience.valueOf(proto.getAlertAudience().name()));

        List<RemoteAttachmentDto> attachments = proto.getRemoteAttachmentList().stream()
                .map(AlertRequestConverter::convertFromRemoteAttachment)
                .collect(Collectors.toList());

        dto.setAttachments(attachments);

        return dto;
    }

    public static RemoteAttachmentDto convertFromRemoteAttachment(RemoteAttachment proto) {
        RemoteAttachmentDto dto = new RemoteAttachmentDto();
        dto.setFileName(proto.getFileName());
        dto.setDownloadUrl(proto.getDownloadUrl());
        return dto;
    }
}