package com.photon.identity.authentication.handler.verificationhandler.impl;

import com.photon.identity.authentication.handler.verificationhandler.api.PhotonAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class PhotonOAuthProvider implements PhotonAuthenticationProvider {
    /**
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    /**
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}