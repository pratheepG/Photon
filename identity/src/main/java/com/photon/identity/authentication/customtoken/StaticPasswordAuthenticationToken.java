package com.photon.identity.authentication.customtoken;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.io.Serial;

public class StaticPasswordAuthenticationToken extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 0;
    private final Object principal;
    private Object credentials;

    public StaticPasswordAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}