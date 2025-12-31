package com.photon.identity.authentication.handler.loginhandler.api;

import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import org.springframework.security.core.Authentication;

public interface PhotonAuthenticationProvider {

    Authentication authenticate(IdentityProviderDto identityProvider, AuthTypeDto authType, Object authRequest) throws ApplicationException;

}