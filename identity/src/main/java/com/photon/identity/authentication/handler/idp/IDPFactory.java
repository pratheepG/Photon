package com.photon.identity.authentication.handler.idp;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.handler.idp.api.IdentityProvider;
import com.photon.identity.commons.enums.IdentityProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IDPFactory {

    private final ApplicationContext context;

    public IDPFactory(ApplicationContext context) {
        this.context = context;
    }

    public IdentityProvider getIdp(IdentityProviderType idpType){
        String service = switch (idpType){
            case LOGIN -> "loginIDP";
            case ON_BOARDING -> "onBoardingIDP";
            default -> throw new ApplicationException(ExceptionEnum.ERR_1033.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE);
        };
        return this.context.getBean(service, IdentityProvider.class);
    }
}