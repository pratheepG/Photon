package com.photon.alerts.provider.mail.sendgrid;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.DownloadedAttachmentDto;
import com.photon.alerts.dto.EmailRequest;
import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.provider.mail.HelperMethods;
import com.photon.alerts.provider.mail.MailProvider;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;
import java.util.List;

@Slf4j
public class SendGridProvider implements MailProvider {

    private final SendGrid sendGrid;

    public SendGridProvider(String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    @Override
    public Mono<AlertProviderResponse> sendEmail(EmailRequest request) {
        List<RemoteAttachmentDto> attachments = request.getAttachments();

        return Flux.fromIterable(attachments != null ? attachments : List.of())
                .flatMap(HelperMethods::downloadAttachment)
                .collectList()
                .flatMap(downloadedAttachments -> Mono.fromCallable(() -> {
                    Mail mail = new Mail(
                            new Email(request.getSender()),
                            request.getSubject(),
                            new Email(request.getRecipient()),
                            new Content(request.isHtml() ? "text/html" : "text/plain", request.getBody())
                    );

                    for (DownloadedAttachmentDto attachment : downloadedAttachments) {
                        Attachments sendGridAttachment = new Attachments();
                        sendGridAttachment.setFilename(attachment.getFileName());
                        sendGridAttachment.setType("application/octet-stream");
                        sendGridAttachment.setDisposition("attachment");

                        String base64Content = Base64.getEncoder().encodeToString(attachment.getFileBytes());
                        sendGridAttachment.setContent(base64Content);

                        mail.addAttachments(sendGridAttachment);
                    }

                    Request sendGridRequest = new Request();
                    sendGridRequest.setMethod(Method.POST);
                    sendGridRequest.setEndpoint("mail/send");
                    sendGridRequest.setBody(mail.build());

                    Response response = sendGrid.api(sendGridRequest);

                    if (response.getStatusCode() >= 400) {
                        throw new RuntimeException("SendGrid error: " + response.getBody());
                    }

                    log.info("✅ SendGrid email sent to {}", request.getRecipient());
                    return AlertProviderResponse.builder()
                            .success(true)
                            .message("Email sent via SendGrid to " + request.getRecipient())
                            .build();
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("❌ SendGrid error: {}", e.getMessage(), e);
                    return Mono.just(AlertProviderResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build());
                });
    }

}