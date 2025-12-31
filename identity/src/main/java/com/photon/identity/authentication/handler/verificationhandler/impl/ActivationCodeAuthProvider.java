package com.photon.identity.authentication.handler.verificationhandler.impl;

import com.photon.identity.authentication.handler.verificationhandler.api.PhotonAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ActivationCodeAuthProvider implements PhotonAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}