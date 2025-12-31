package com.photon.alerts.provider.sms.messagebird;

import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.objects.MessageResponse;
import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.provider.sms.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.util.List;

@Slf4j
public class MessageBirdProvider implements SmsProvider {

    private final MessageBirdClient client;

    public MessageBirdProvider(String accessKey) {
        MessageBirdService service = new MessageBirdServiceImpl(accessKey);
        this.client = new MessageBirdClient(service);
    }

    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        return Mono.fromCallable(() -> {
            MessageResponse response = client.sendMessage(
                    request.getFrom(),
                    request.getMessage(),
                    List.of(BigInteger.valueOf(Long.parseLong(request.getTo())))
            );

            return AlertProviderResponse.builder()
                    .success(true)
                    .message("MessageBird SMS ID: " + response.getId())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> Mono.just(AlertProviderResponse.builder()
                  .success(false)
                  .message("MessageBird Error: " + e.getMessage())
                  .build()));
    }
}