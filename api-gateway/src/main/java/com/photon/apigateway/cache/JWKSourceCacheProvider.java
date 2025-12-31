package com.photon.apigateway.cache;

import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.photon.apigateway.utils.LoadBalancedResourceRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Lazy
@Slf4j
@Component
public class JWKSourceCacheProvider {

    private JWKSource<SecurityContext> jwkSource;
    private final LoadBalancedResourceRetriever retriever;

    public JWKSourceCacheProvider(LoadBalancedResourceRetriever retriever) {
        this.retriever = retriever;
    }

    public synchronized JWKSource<SecurityContext> getJwkSource() throws IOException {
        if (this.jwkSource == null) {
            try {
                URL dummyUrl = new URL("http://dummy.local/.well-known/jwks.json");
                this.jwkSource = JWKSourceBuilder.create(dummyUrl, retriever).build();
            } catch (Exception e) {
                log.error("Failed to initialize JWKSource, retrying on next request.", e);
                throw new IOException("Failed to initialize JWKSource", e);
            }
        }
        return this.jwkSource;
    }

    public synchronized void refreshJwkSource() throws IOException {
        this.jwkSource = null;
        this.getJwkSource();
    }
}