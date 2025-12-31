package com.photon.alerts.dto.request;

import com.photon.alerts.enums.AlertAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRequestDto {
    private String alertType;
    private String alertSubType;
    private AlertAudience audience;
    private String topic;
    private List<AlertTemplateRequestDto> templates;
}