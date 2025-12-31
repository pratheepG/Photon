package com.photon.identity.authentication.customtoken;

import com.photon.identity.authentication.entity.TempCredential;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class EmailOtpAuthenticationToken extends AbstractAuthenticationToken {

    private final String securityKey;
    private String otp;
    private Object principal;
    @Getter
    private TempCredential tempCredential;

    public EmailOtpAuthenticationToken(TempCredential tempCredential, String securityKey, String otp) {
        super(null);
        this.tempCredential = tempCredential;
        this.securityKey = securityKey;
        this.otp = otp;
        setAuthenticated(false);
    }

    public EmailOtpAuthenticationToken(Object principle, TempCredential tempCredential, String securityKey, String otp) {
        super(null);
        this.principal = principle;
        this.tempCredential = tempCredential;
        this.securityKey = securityKey;
        this.otp = otp;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.otp;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public Object getSecurityKey() {
        return this.securityKey;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        otp = null;
    }

}