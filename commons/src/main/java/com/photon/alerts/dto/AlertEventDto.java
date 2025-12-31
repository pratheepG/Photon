package com.photon.alerts.dto;

import com.photon.alerts.enums.AlertAudience;
import com.photon.alerts.enums.Channel;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEventDto {
    private String alertType;
    private String alertSubType;
    private String status;
    private Map<String, String> placeholders;
    private boolean isPassword;

    private List<RemoteAttachmentDto> attachments;

    private String smsDeeplinkUrl;
    private String smsMediaUrl;

    private boolean isPreLogin;
    private String phoneNumber;
    private String countryCode;
    private String email;

    private Channel channel;
    private AlertAudience alertAudience;
    private String userId;
    private String topic;
}