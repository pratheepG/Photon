package com.photon.alerts.provider.mail.smtp;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.DownloadedAttachmentDto;
import com.photon.alerts.dto.EmailRequest;
import com.photon.alerts.provider.mail.HelperMethods;
import com.photon.alerts.provider.mail.MailProvider;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SmtpProvider implements MailProvider {

    private final Session session;
    private final String fromEmail;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public SmtpProvider(String host, int port, String username, String password, boolean tls) {
        this.fromEmail = username;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public Mono<AlertProviderResponse> sendEmail(EmailRequest emailRequest) {
        return Flux.fromIterable(emailRequest.getAttachments() != null ? emailRequest.getAttachments() : List.of())
                .flatMap(HelperMethods::downloadAttachment)
                .collectList()
                .flatMap(downloadedAttachments -> Mono.fromCallable(() -> {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailRequest.getRecipient()));
                    message.setSubject(emailRequest.getSubject());

                    if (!downloadedAttachments.isEmpty()) {
                        MimeBodyPart textPart = new MimeBodyPart();
                        textPart.setContent(emailRequest.getBody(), emailRequest.isHtml() ? "text/html" : "text/plain");

                        Multipart multipart = new MimeMultipart();
                        multipart.addBodyPart(textPart);

                        for (DownloadedAttachmentDto attachment : downloadedAttachments) {
                            MimeBodyPart attachmentPart = new MimeBodyPart();
                            attachmentPart.setFileName(attachment.getFileName());
                            attachmentPart.setContent(attachment.getFileBytes(), "application/octet-stream");
                            multipart.addBodyPart(attachmentPart);
                        }

                        message.setContent(multipart);
                    } else {
                        message.setContent(emailRequest.getBody(), emailRequest.isHtml() ? "text/html" : "text/plain");
                    }

                    Transport.send(message);
                    log.info("✅ Email sent to {}", emailRequest.getRecipient());

                    return AlertProviderResponse.builder()
                            .success(true)
                            .message("Email sent to " + emailRequest.getRecipient())
                            .build();
                }))
                .subscribeOn(Schedulers.fromExecutor(executor))
                .onErrorResume(e -> {
                    log.error("❌ SMTP Send failed to {}: {}", emailRequest.getRecipient(), e.getMessage(), e);
                    return Mono.just(AlertProviderResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build());
                });
    }
}