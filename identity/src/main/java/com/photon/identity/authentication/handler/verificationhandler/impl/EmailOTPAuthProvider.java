package com.photon.identity.authentication.handler.verificationhandler.impl;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.customtoken.EmailOtpAuthenticationToken;
import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.authentication.handler.verificationhandler.api.PhotonAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class EmailOTPAuthProvider implements PhotonAuthenticationProvider {

    private final PasswordEncoder passwordEncoder;

    public EmailOTPAuthProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param authentication Authentication
     * @return Authentication
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        EmailOtpAuthenticationToken emailOtpAuthenticationToken = (EmailOtpAuthenticationToken) authentication;
        final String otp = (String) emailOtpAuthenticationToken.getCredentials();
        final TempCredential tempCredential = emailOtpAuthenticationToken.getTempCredential();

        authentication.setAuthenticated(validateOtp(otp, tempCredential));
        return authentication;
    }

    private boolean validateOtp(String otp, TempCredential tempCredential) {
        if (!this.passwordEncoder.matches(otp, tempCredential.getId())) {
            log.debug("Failed to authenticate since password does not match stored value");
            throw new ApplicationException(ExceptionEnum.ERR_1030.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
        }
        return true;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return EmailOtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}