package com.photon.alerts.provider.sms;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import reactor.core.publisher.Mono;

public interface SmsProvider {
    Mono<AlertProviderResponse> sendSms(SmsRequest request);
}
