package com.photon.alerts.provider.mail.console;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.EmailRequest;
import com.photon.alerts.provider.mail.MailProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import reactor.core.publisher.Mono;

@Slf4j
public class ConsoleMailProvider implements MailProvider {
    @Override
    public Mono<AlertProviderResponse> sendEmail(EmailRequest request) throws MessagingException {
        return Mono.fromRunnable(() -> log.info("📟 EMAIL to [{}]: {}", request.getRecipient(), request.getBody()))
                .thenReturn(AlertProviderResponse.builder().success(true).message("Console EMAIL logged").build());
    }
}