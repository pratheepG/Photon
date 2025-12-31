package com.photon.identity.authentication.handler.loginhandler;

import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.authentication.handler.loginhandler.api.PhotonAuthenticationProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PhotonAuthenticationProviderFactory {

    private final ApplicationContext context;

    public PhotonAuthenticationProviderFactory(ApplicationContext context) {
        this.context = context;
    }

    public PhotonAuthenticationProvider getAuthenticationProvider(AuthAdaptor authAdaptor) {
        String serviceName = switch (authAdaptor) {
            case SMS_OTP -> "smsOtpAuthenticationProvider";
            case EML_OTP -> "emailOtpAuthenticationProvider";
            case STATIC_PWD, TMP_PWD -> "staticPasswordAuthenticationProvider";
            default -> "staticPasswordAuthenticationProvider";
        };
        return this.context.getBean(serviceName, PhotonAuthenticationProvider.class);
    }
}