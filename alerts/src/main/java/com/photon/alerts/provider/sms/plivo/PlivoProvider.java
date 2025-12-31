package com.photon.alerts.provider.sms.plivo;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.provider.sms.SmsProvider;
import com.plivo.api.Plivo;
import com.plivo.api.models.message.Message;
import com.plivo.api.models.message.MessageCreateResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
public class PlivoProvider implements SmsProvider {

    public PlivoProvider(String authId, String authToken) {
        Plivo.init(authId, authToken);
    }

    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        return Mono.fromCallable(() -> {
            MessageCreateResponse response = Message.creator(
                    request.getFrom(),
                    List.of(request.getTo()),
                    request.getMessage()
            ).create();

            return AlertProviderResponse.builder()
                    .success(true)
                    .message("Plivo SMS ID: " + response.getMessageUuid())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> Mono.just(AlertProviderResponse.builder()
                  .success(false)
                  .message("Plivo Error: " + e.getMessage())
                  .build()));
    }
}
