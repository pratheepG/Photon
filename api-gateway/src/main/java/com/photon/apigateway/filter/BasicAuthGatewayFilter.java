package com.photon.apigateway.filter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJWSException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.ExpiredJWTException;
import com.photon.apigateway.cache.JWKSourceCacheProvider;
import com.photon.dto.JwtHeaderDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import com.photon.utils.HttpRequestManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class BasicAuthGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    private final JWKSourceCacheProvider jwkSourceCacheProvider;
    private final ApplicationConfigProperties applicationConfigProperties;

    public BasicAuthGatewayFilter(JWKSourceCacheProvider jwkSourceCacheProvider, ApplicationConfigProperties applicationConfigProperties) {
        super(Object.class);
        this.jwkSourceCacheProvider = jwkSourceCacheProvider;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            try {
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || (!authHeader.startsWith("Basic ") && !authHeader.startsWith("Bearer "))) {
                    return unauthorized(exchange);
                }
                if(authHeader.startsWith("Bearer ")){
                    String token = authHeader.substring(7);
                    JwtHeaderDto header = HttpRequestManager.readJwtHeader(authHeader);
                    ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                    JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.parse(header.getAlg()), this.jwkSourceCacheProvider.getJwkSource());
                    jwtProcessor.setJWSKeySelector(keySelector);
                    jwtProcessor.process(token, null);
                    return chain.filter(exchange);
                }

                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
                String[] values = credentials.split(":", 2);
                if (values.length != 2) {
                    return unauthorized(exchange);
                }

                String username = values[0];
                String password = values[1];

                if (!(safeEquals(applicationConfigProperties.getCompositeXApiKey(), username) && safeEquals(applicationConfigProperties.getCompositeXApiSecret(), password))) {
                    return unauthorized(exchange);
                }
                return chain.filter(exchange);
            } catch (BadJWSException e) {
                log.error("Invalid JWT token: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1037.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } catch (ExpiredJWTException e) {
                log.error("JWT Token Expired: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("Failed to validate the request: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1037.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange ex) {
        ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        throw new ApplicationException(ExceptionEnum.ERR_1043.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int r = 0;
        for (int i = 0; i < a.length(); i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }
}