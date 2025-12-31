package com.photon.alerts.provider.sms.rest;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.provider.sms.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class RestSmsProvider implements SmsProvider {

    private final WebClient webClient;

    public RestSmsProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        String endpoint = request.getMetadata().get("providerEndpoint");
        return webClient.post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .map(r -> AlertProviderResponse.builder().success(true).message("REST SMS sent").build())
                .onErrorResume(e -> Mono.just(AlertProviderResponse.builder().success(false).message("REST error: " + e.getMessage()).build()));
    }
}
