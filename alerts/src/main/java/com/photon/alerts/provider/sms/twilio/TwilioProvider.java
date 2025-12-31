package com.photon.alerts.provider.sms.twilio;

import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.SmsRequest;
import com.photon.alerts.provider.sms.SmsProvider;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class TwilioProvider implements SmsProvider {

    private final String twilioPhoneNumber;

    public TwilioProvider(String accountSid, String authToken, String twilioPhoneNumber) {
        this.twilioPhoneNumber = twilioPhoneNumber;
        Twilio.init(accountSid, authToken);
    }

    @Override
    public Mono<AlertProviderResponse> sendSms(SmsRequest request) {
        return Mono.fromCallable(() -> {
                    Message message = Message.creator(
                            new PhoneNumber("+".concat(request.getCountryCode()).concat(request.getTo())),
                            new PhoneNumber(twilioPhoneNumber),
                            request.getMessage()
                    ).create();

                    log.info("✅ Twilio SMS sent: SID={}", message.getSid());
                    return AlertProviderResponse.builder()
                            .success(true)
                            .message("Twilio SMS sent")
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("❌ Twilio SMS failed: {}", e.getMessage(), e);
                    return Mono.just(AlertProviderResponse.builder()
                            .success(false)
                            .message("Twilio error: " + e.getMessage())
                            .build());
                });
    }
}