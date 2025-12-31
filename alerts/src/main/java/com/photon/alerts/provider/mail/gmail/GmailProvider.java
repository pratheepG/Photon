package com.photon.alerts.provider.mail.gmail;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.DownloadedAttachmentDto;
import com.photon.alerts.dto.EmailRequest;
import com.photon.alerts.dto.RemoteAttachmentDto;
import com.photon.alerts.enums.AlertStatus;
import com.photon.alerts.provider.mail.HelperMethods;
import com.photon.alerts.provider.mail.MailProvider;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class GmailProvider implements MailProvider {

    private final JavaMailSenderImpl mailSender;

    public GmailProvider(String username, String password) {
        this.mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    @Override
    public Mono<AlertProviderResponse> sendEmail(EmailRequest request) {
        List<RemoteAttachmentDto> attachments = request.getAttachments();

        if (attachments == null || attachments.isEmpty()) {
            return sendEmailWithAttachments(request, new ArrayList<>());
        }

        return Flux.fromIterable(attachments)
                .flatMap(HelperMethods::downloadAttachment)
                .collectList()
                .flatMap(downloaded -> sendEmailWithAttachments(request, downloaded));
    }

    private Mono<AlertProviderResponse> sendEmailWithAttachments(EmailRequest request, List<DownloadedAttachmentDto> attachments) {
        return Mono.fromCallable(() -> {
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);

                    helper.setTo(request.getRecipient());
                    helper.setSubject(request.getSubject());
                    helper.setText(request.getBody(), true);

                    for (DownloadedAttachmentDto attachment : attachments) {
                        helper.addAttachment(
                                attachment.getFileName(),
                                new ByteArrayResource(attachment.getFileBytes())
                        );
                    }

                    mailSender.send(message);
                    log.info("📧 Sent email to {}", request.getRecipient());

                    return AlertProviderResponse.builder()
                            .alertStatus(AlertStatus.DELIVERED)
                            .success(true)
                            .message("Email sent successfully to " + request.getRecipient())
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("❌ Failed to send email to {}: {}", request.getRecipient(), e.getMessage(), e);
                    return Mono.just(AlertProviderResponse.builder()
                            .alertStatus(AlertStatus.FAILED)
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build());
                });
    }
}