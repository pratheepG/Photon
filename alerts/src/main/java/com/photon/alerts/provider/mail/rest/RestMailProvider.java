//package com.photon.alerts.provider.mail.rest;
//
//import com.photon.alerts.dto.AlertProviderResponse;
//import com.photon.alerts.dto.EmailRequest;
//import com.photon.alerts.provider.mail.MailProvider;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@Slf4j
//public class RestMailProvider implements MailProvider {
//
//    private final WebClient webClient;
//
//    public RestMailProvider(WebClient webClient) {
//        this.webClient = webClient;
//    }
//
//    @Override
//    public Mono<AlertProviderResponse> sendEmail(EmailRequest emailRequest) {
//        String fullUrl = emailRequest.getMetadata().get("restEndpoint"); // Just an example!
//
//        log.info("🌐 Sending email via dynamic REST endpoint: {}", fullUrl);
//
//        return webClient.post()
//                .uri(fullUrl)
//                .bodyValue(Map.of(
//                        "to", emailRequest.getRecipient(),
//                        "subject", emailRequest.getSubject(),
//                        "body", emailRequest.getBody(),
//                        "isHtml", emailRequest.isHtml()
//                ))
//                .retrieve()
//                .toBodilessEntity()
//                .map(response -> AlertProviderResponse.builder()
//                        .success(true)
//                        .message("Email sent to " + emailRequest.getRecipient())
//                        .build())
//                .onErrorResume(e -> {
//                    log.error("❌ REST call failed", e);
//                    return Mono.just(AlertProviderResponse.builder()
//                            .success(false)
//                            .message("Failed to send: " + e.getMessage())
//                            .build());
//                });
//    }
//}