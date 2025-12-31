package com.photon.alerts.provider.sms;

import com.photon.alerts.provider.sms.console.ConsoleSmsProvider;
import com.photon.alerts.provider.sms.messagebird.MessageBirdProvider;
import com.photon.alerts.provider.sms.plivo.PlivoProvider;
import com.photon.alerts.provider.sms.rest.RestSmsProvider;
import com.photon.alerts.provider.sms.twilio.TwilioProvider;
import com.photon.alerts.provider.sms.vonage.VonageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RefreshScope
public class SmsProviderFactory {

    @Value("${sms.provider:}")
    private String provider;

    @Value("${twilio.sid:}")
    private String twilioSid;

    @Value("${twilio.token:}")
    private String twilioToken;

    @Value("${twilio.phonenumber:}")
    private String twilioPhoneNumber;

    @Value("${vonage.apiKey:}")
    private String vonageKey;

    @Value("${vonage.apiSecret:}")
    private String vonageSecret;

    @Value("${plivo.authId:}")
    private String plivoId;

    @Value("${plivo.authToken:}")
    private String plivoToken;

    @Value("${messagebird.accessKey:}")
    private String messageBirdToken;

    private final WebClient webClient;

    public SmsProviderFactory(WebClient webClient) {
        this.webClient = webClient;
    }

    public SmsProvider getProvider() {
        log.info("SmsProvider Set to : {}",provider);
        return switch (provider.toLowerCase()) {
            case "twilio"      -> new TwilioProvider(twilioSid, twilioToken, twilioPhoneNumber);
            case "vonage"      -> new VonageProvider(vonageKey, vonageSecret);
            case "plivo"       -> new PlivoProvider(plivoId, plivoToken);
            case "messagebird" -> new MessageBirdProvider(messageBirdToken);
            case "rest"        -> new RestSmsProvider(webClient);
            default            -> new ConsoleSmsProvider();
        };
    }

}