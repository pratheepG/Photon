package com.photon.identity.authentication.handler.verificationhandler.impl;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.customtoken.StaticPasswordAuthenticationToken;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.handler.verificationhandler.api.PhotonAuthenticationProvider;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;


@Slf4j
public class StaticPasswordAuthProvider implements PhotonAuthenticationProvider {

    private final PasswordEncoder passwordEncoder;

    public StaticPasswordAuthProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws ApplicationException {

        if (authentication.getCredentials() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new ApplicationException(ExceptionEnum.ERR_1043.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }

        StaticPasswordAuthenticationToken token = (StaticPasswordAuthenticationToken) authentication;
        final User user = (User) token.getPrincipal();
        final String presentedPassword = authentication.getCredentials().toString();

        if(!user.isEnabled())
            throw new ApplicationException(ExceptionEnum.ERR_1021.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);

        if (!this.passwordEncoder.matches(presentedPassword, user.getPassword())) {
            throw new ApplicationException(ExceptionEnum.ERR_1030.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }

        authentication.setAuthenticated(true);
        return authentication;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return StaticPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}