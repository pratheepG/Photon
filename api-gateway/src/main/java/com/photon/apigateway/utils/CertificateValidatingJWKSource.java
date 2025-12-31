package com.photon.apigateway.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A custom JWKSource that wraps an existing JWKSource to validate
 * the expiration date of the underlying X.509 certificate.
 * If a certificate is expired, it will be excluded from the returned JWKSet.
 */
@Slf4j
public class CertificateValidatingJWKSource implements JWKSource<SecurityContext> {

    private final JWKSource<SecurityContext> delegate;

    public CertificateValidatingJWKSource(JWKSource<SecurityContext> delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        List<JWK> selectedJwks = delegate.get(jwkSelector, context);
        return selectedJwks.stream()
            .filter(this::isCertificateValid)
            .collect(Collectors.toList());
    }

    private boolean isCertificateValid(JWK jwk) {
        if (jwk.getX509CertChain() == null || jwk.getX509CertChain().isEmpty()) {
            return true;
        }
        try {
            X509Certificate cert = jwk.getParsedX509CertChain().get(0);
            if (cert == null) {
                return true;
            }
            cert.checkValidity(new Date());
            return true;
        } catch (CertificateExpiredException e) {
            log.error("Found an expired signing certificate for key ID: {}. Rejecting this JWK.", jwk.getKeyID(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1046.getErrorResponseBody("JWT Token is signed with expired certificate."), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Failed to validate certificate for key ID: {}. Assuming it's valid for this check.", jwk.getKeyID(), e);
            return true;
        }
    }
}