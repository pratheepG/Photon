package com.photon.alerts.dto;

import com.photon.alerts.enums.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertProviderResponse {
    private boolean success;
    private String message;
    private AlertStatus alertStatus;
    private Long errCode;
}