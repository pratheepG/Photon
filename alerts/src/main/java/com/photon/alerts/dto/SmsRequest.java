package com.photon.alerts.dto;

import com.photon.alerts.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String to;
    private String from;
    private String message;
    private String countryCode;
    private String deeplinkUrl;
    private String mediaUrl;
    private boolean isOTP;
    private MessageType messageType;
    private Map<String, String> metadata;
}