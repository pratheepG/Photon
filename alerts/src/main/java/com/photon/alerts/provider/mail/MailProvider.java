package com.photon.alerts.provider.mail;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.EmailRequest;
import org.springframework.messaging.MessagingException;
import reactor.core.publisher.Mono;

public interface MailProvider {
    Mono<AlertProviderResponse> sendEmail(EmailRequest emailRequest) throws MessagingException;
}