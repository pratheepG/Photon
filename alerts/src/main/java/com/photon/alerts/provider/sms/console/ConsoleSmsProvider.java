package com.photon.alerts.provider.sms.console;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.enums.AlertStatus;
import com.photon.alerts.provider.sms.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ConsoleSmsProvider implements SmsProvider {
    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        return Mono.fromRunnable(() -> log.info("📟 SMS to [{}]: {}", request.getTo(), request.getMessage()))
                   .thenReturn(AlertProviderResponse.builder().success(true).alertStatus(AlertStatus.DELIVERED).message("Console SMS logged").build());
    }
}