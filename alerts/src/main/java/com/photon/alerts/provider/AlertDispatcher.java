package com.photon.alerts.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.alerts.AlertRequestConverter;
import com.photon.alerts.dto.*;
import com.photon.alerts.dto.response.AlertResponseDto;
import com.photon.alerts.dto.response.AlertTemplateResponseDto;
import com.photon.alerts.entity.*;
import com.photon.alerts.enums.AlertStatus;
import com.photon.alerts.enums.Channel;
import com.photon.alerts.proto.AlertRequest;
import com.photon.alerts.provider.mail.MailProviderFactory;
import com.photon.alerts.provider.sms.SmsProviderFactory;
import com.photon.alerts.repository.AlertHistoryRepository;
import com.photon.alerts.utils.TemplateProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AlertDispatcher {

    private final MailProviderFactory mailProviderFactory;
    private final SmsProviderFactory smsProviderFactory;
    private final AlertHistoryRepository alertHistoryRepository;
    private final ObjectMapper objectMapper;

    public AlertDispatcher(MailProviderFactory mailProviderFactory, SmsProviderFactory smsProviderFactory, AlertHistoryRepository alertHistoryRepository, ObjectMapper objectMapper) {
        this.mailProviderFactory = mailProviderFactory;
        this.smsProviderFactory = smsProviderFactory;
        this.alertHistoryRepository = alertHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> dispatch(AlertResponseDto alert, Subscriber subscriber, AlertRequest alertRequest) {
        return Flux.fromIterable(alert.getTemplates())
                .flatMap(template -> switch (template.getChannel()) {
                    case SMS -> this.sendSMS(alert, template, subscriber, alertRequest);
                    case E_MAIL -> this.sendMail(alert, template, subscriber, alertRequest);
                    default -> Mono.empty();
                })
                .then();
    }


    public Mono<Void> dispatch(AlertResponseDto alert, AlertRequest alertRequest) {

        List<AlertTemplateResponseDto> templatesToDispatch;

        if (alertRequest.getChannel().equals(com.photon.alerts.proto.Channel.ALL)) {
            templatesToDispatch = alert.getTemplates();
        } else {
            templatesToDispatch = alert.getTemplates().stream()
                    .filter(template -> template.getChannel().equals(com.photon.alerts.enums.Channel.valueOf(alertRequest.getChannel().name())))
                    .collect(Collectors.toList());
        }

        return Flux.fromIterable(templatesToDispatch)
                .flatMap(template -> {
                    Mono<Void> dispatchMono;

                        switch (template.getChannel()) {
                            case SMS:
                                String deeplinkUrl = StringUtils.isEmpty(alertRequest.getSmsDeeplinkUrl())
                                        ? StringUtils.defaultIfEmpty(template.getSmsDeeplinkUrl(), "")
                                        : alertRequest.getSmsDeeplinkUrl();

                                String mediaUrl = StringUtils.isEmpty(alertRequest.getSmsMediaUrl())
                                        ? StringUtils.defaultIfEmpty(template.getSmsMediaUrl(), "")
                                        : alertRequest.getSmsMediaUrl();

                                SmsRequest smsRequest = SmsRequest.builder()
                                        .to(alertRequest.getPhoneNumber())
                                        .countryCode(alertRequest.getCountryCode())
                                        .isOTP(alertRequest.getIsPassword())
                                        .message(TemplateProcessor.resolveTemplate(template.getMessageTemplate(), alertRequest.getPlaceholdersMap()))
                                        .deeplinkUrl(deeplinkUrl)
                                        .mediaUrl(mediaUrl)
                                        .build();

                                dispatchMono = sendSMS(smsRequest, alert);
                                break;

                            case E_MAIL:
                                AlertEventDto alertEventDto = AlertRequestConverter.convertFromProtobuf(alertRequest);
                                List<RemoteAttachmentDto> remoteAttachments = alertEventDto.getAttachments().isEmpty()
                                        ? template.getMailAttachments()
                                        : alertEventDto.getAttachments();

                                EmailRequest emailRequest = EmailRequest.builder()
                                        .recipient(alertRequest.getEmail())
                                        .body(TemplateProcessor.resolveTemplate(template.getMessageTemplate(), alertRequest.getPlaceholdersMap()))
                                        .subject(TemplateProcessor.resolveTemplate(template.getSubjectTemplate(), alertRequest.getPlaceholdersMap()))
                                        .attachments(remoteAttachments)
                                        .build();

                                dispatchMono = sendMail(emailRequest, alert);
                                break;

                            default:
                                log.warn("Unknown channel: {}", template.getChannel());
                                dispatchMono = Mono.empty();
                        }
                    return dispatchMono;
                })
                .then();
    }


    private Mono<Void> sendSMS(AlertResponseDto alert, AlertTemplateResponseDto template, Subscriber subscriber, AlertRequest alertRequest) {
        String deeplinkUrl = StringUtils.isEmpty(alertRequest.getSmsDeeplinkUrl())? StringUtils.isEmpty(template.getSmsDeeplinkUrl())?"":template.getSmsDeeplinkUrl() : alertRequest.getSmsDeeplinkUrl();
        String mediaUrl = StringUtils.isEmpty(alertRequest.getSmsMediaUrl())? StringUtils.isEmpty(template.getSmsMediaUrl())?"":template.getSmsMediaUrl() : alertRequest.getSmsMediaUrl();
        SmsRequest smsRequest = SmsRequest.builder()
                .to(subscriber.getPhoneNumber())
                .countryCode(subscriber.getCountryCode())
                .isOTP(alertRequest.getIsPassword())
                .message(TemplateProcessor.resolveTemplate(template.getMessageTemplate(), alertRequest.getPlaceholdersMap()))
                .build();

        if (!StringUtils.isEmpty(deeplinkUrl))
            smsRequest.setDeeplinkUrl(deeplinkUrl);

        if (!StringUtils.isEmpty(mediaUrl))
            smsRequest.setMediaUrl(mediaUrl);

        return this.sendSMS(smsRequest, alert);

    }

    private Mono<Void> sendMail(AlertResponseDto alert, AlertTemplateResponseDto template, Subscriber subscriber, AlertRequest alertRequest) {
        AlertEventDto alertEventDto = AlertRequestConverter.convertFromProtobuf(alertRequest);
        List<RemoteAttachmentDto> remoteAttachments = alertEventDto.getAttachments().isEmpty()? template.getMailAttachments():
                alertEventDto.getAttachments();

        EmailRequest emailRequest = EmailRequest.builder()
                .recipient(subscriber.getEmail())
                .body(TemplateProcessor.resolveTemplate(template.getMessageTemplate(), alertRequest.getPlaceholdersMap()))
                .subject(TemplateProcessor.resolveTemplate(template.getSubjectTemplate() , alertRequest.getPlaceholdersMap()))
                .build();

        if (remoteAttachments != null && !remoteAttachments.isEmpty())
            emailRequest.setAttachments(remoteAttachments);

        return this.sendMail(emailRequest, alert);

    }

    public Mono<Void> sendSMS(SmsRequest smsRequest, AlertResponseDto alert) {
        String json;
        try {
            json = this.objectMapper.writeValueAsString(alert);
            log.debug("SMS Alert JSON: {}", json);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize alert", e));
        }

        return smsProviderFactory.getProvider().sendSms(smsRequest)
                .flatMap(result -> {
                    AlertHistory history = AlertHistory.builder()
                            .alertId(alert.getId().toString())
                            .payload(json)
                            .channel(Channel.SMS.name())
                            .status(result.getAlertStatus())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return alertHistoryRepository.save(history).then();
                })
                .onErrorResume(e -> {
                    AlertHistory history = AlertHistory.builder()
                            .alertId(alert.getId().toString())
                            .payload(json)
                            .channel(Channel.SMS.name())
                            .status(AlertStatus.FAILED)
                            .createdAt(LocalDateTime.now())
                            .failureReason(e.getMessage())
                            .build();
                    return alertHistoryRepository.save(history).then();
                });
    }

    public Mono<Void> sendMail(EmailRequest emailRequest, AlertResponseDto alert) {
        String json;
        try {
            json = this.objectMapper.writeValueAsString(alert);
            log.debug("Email Alert JSON: {}", json);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize alert", e));
        }

        return mailProviderFactory.getProvider().sendEmail(emailRequest)
                .flatMap(result -> {
                    AlertHistory history = AlertHistory.builder()
                            .alertId(alert.getId().toString())
                            .payload(json)
                            .channel(Channel.E_MAIL.name())
                            .status(result.getAlertStatus())
                            .createdAt(LocalDateTime.now())
                            .build();
                    return alertHistoryRepository.save(history).then();
                })
                .onErrorResume(e -> {
                    AlertHistory history = AlertHistory.builder()
                            .alertId(alert.getId().toString())
                            .payload(json)
                            .channel(Channel.E_MAIL.name())
                            .status(AlertStatus.FAILED)
                            .createdAt(LocalDateTime.now())
                            .failureReason(e.getMessage())
                            .build();
                    return alertHistoryRepository.save(history).then();
                });
    }

    public Mono<Alert> sendPushNotification(Alert alert) {
        return Mono.just(alert);
    }

}