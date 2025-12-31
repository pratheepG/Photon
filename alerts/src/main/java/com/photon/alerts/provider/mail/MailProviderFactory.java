package com.photon.alerts.provider.mail;

import com.photon.alerts.provider.mail.console.ConsoleMailProvider;
import com.photon.alerts.provider.mail.gmail.GmailProvider;
import com.photon.alerts.provider.mail.outlook.OutlookProvider;
import com.photon.alerts.provider.mail.sendgrid.SendGridProvider;
import com.photon.alerts.provider.mail.smtp.SmtpProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RefreshScope
public class MailProviderFactory {

    @Value("${mail.provider:}")
    private String providerType;

    @Value("${gmail.username:}")
    private String username;

    @Value("${gmail.password:}")
    private String password;

    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;

    @Value("${outlook.client.id:}")
    private String outlookClientId;

    @Value("${outlook.client.secret:}")
    private String outlookClientSecret;

    @Value("${outlook.tenant.id:}")
    private String outlookTenantId;

    @Value("${outlook.auth.url:}")
    private String outlookAuthUrl;

    @Value("${email.smtp.host:}")
    private String smtpHost;

    @Value("${email.smtp.port:0000}")
    private int smtpPort;

    @Value("${email.smtp.username:}")
    private String smtpUserName;

    @Value("${email.smtp.password:}")
    private String smtpPassword;

    @Value("${email.smtp.tls:false}")
    private boolean smtpTLS;

    public MailProvider getProvider() {
        log.info("MailProvider Set to : {}",providerType);
        return switch (providerType.toLowerCase()) {
            case "gmail" -> new GmailProvider(username, password);
            case "sendgrid" -> new SendGridProvider(sendGridApiKey);
            case "outlook" -> new OutlookProvider(outlookClientId, outlookClientSecret, outlookTenantId, outlookAuthUrl);
            case "smtp" -> new SmtpProvider(smtpHost, smtpPort, smtpUserName, smtpPassword, smtpTLS);
            default -> new ConsoleMailProvider();
        };
    }
}