package com.photon.identity.authentication.handler.otphandler;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.handler.otphandler.api.OtpHandler;
import com.photon.identity.commons.enums.IdentityProviderType;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OtpFactory {
    private final ApplicationContext context;

    public OtpFactory(ApplicationContext context) {
        this.context = context;
    }

    public OtpHandler getOtpHandler(IdentityProviderType idpType) {
        String service = switch (idpType){
            case LOGIN -> "loginOtpHandler";
            case ON_BOARDING -> "onboardingOtpHandler";
            default -> throw new ApplicationException(ExceptionEnum.ERR_1033.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE);
        };

        return this.context.getBean(service, OtpHandler.class);
    }

}