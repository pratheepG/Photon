package com.photon.alerts.provider.sms.vonage;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.provider.sms.SmsProvider;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class VonageProvider implements SmsProvider {

    private final VonageClient vonageClient;

    public VonageProvider(String apiKey, String apiSecret) {
        this.vonageClient = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
    }

    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        return Mono.fromCallable(() -> {
            TextMessage message = new TextMessage(
                    request.getFrom(),
                    request.getTo(),
                    request.getMessage()
            );

            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);
            if (response.getMessages().get(0).getStatus() != MessageStatus.OK) {
                throw new RuntimeException("Vonage SMS failed: " + response.getMessages().get(0).getErrorText());
            }

            return AlertProviderResponse.builder()
                    .success(true)
                    .message("Vonage SMS sent")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> Mono.just(AlertProviderResponse.builder()
                  .success(false)
                  .message("Vonage Error: " + e.getMessage())
                  .build()));
    }
}
