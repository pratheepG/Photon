package com.photon.identity.authentication.handler.verificationhandler.impl;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.handler.verificationhandler.api.PhotonAuthenticationProvider;
import com.photon.identity.authentication.customtoken.SmsOtpAuthenticationToken;
import com.photon.identity.authentication.entity.TempCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class SmsOTPAuthProvider implements PhotonAuthenticationProvider {

    private final PasswordEncoder passwordEncoder;

    public SmsOTPAuthProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param authentication Authentication
     * @return Authentication
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        SmsOtpAuthenticationToken smsOtpAuthenticationToken = (SmsOtpAuthenticationToken) authentication;
        final String otp = (String) smsOtpAuthenticationToken.getCredentials();
        final TempCredential tempCredential = smsOtpAuthenticationToken.getTempCredential();

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
        return SmsOtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}