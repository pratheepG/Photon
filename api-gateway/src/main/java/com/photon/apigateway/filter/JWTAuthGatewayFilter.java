package com.photon.apigateway.filter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJWSException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.ExpiredJWTException;

import com.photon.apigateway.cache.JWKSourceCacheProvider;
import com.photon.apigateway.utils.CertificateValidatingJWKSource;
import com.photon.dto.JwtHeaderDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.utils.HttpRequestManager;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.server.ServerWebExchange;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Component
public class JWTAuthGatewayFilter extends AbstractGatewayFilterFactory<JWTAuthGatewayFilter.Config> {

    private final JWKSourceCacheProvider jwkSourceCacheProvider;

    public JWTAuthGatewayFilter(JWKSourceCacheProvider jwkSourceCacheProvider) {
        super(Config.class);
        this.jwkSourceCacheProvider = jwkSourceCacheProvider;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("roles", "idps");
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private String roles;
        private String idps;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                log.error("Auth token not found in header");
                throw new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } else if (!authHeader.startsWith("Bearer ")) {
                log.error("Invalid token format { Not a bearer token }");
                throw new ApplicationException(ExceptionEnum.ERR_1037.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);
            try {
                JwtHeaderDto header = HttpRequestManager.readJwtHeader(authHeader);
                ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                JWKSource<SecurityContext> validatingJwkSource = new CertificateValidatingJWKSource(this.jwkSourceCacheProvider.getJwkSource());
                JWSVerificationKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.parse(header.getAlg()), validatingJwkSource);
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSet claims = jwtProcessor.process(token, null);

                Set<String> allowedRoles = Arrays.stream(Optional.ofNullable(config.getRoles()).orElse("").split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
                Set<String> allowedIdps = Arrays.stream(Optional.ofNullable(config.getIdps()).orElse("").split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());

                List<String> userRoles = claims.getStringListClaim("roles");
                String userIdp = header.getIdp();

                if(userIdp.contains("SC_IDP") && userRoles.contains("SERVER_CONSOLE_ADMIN_ROLE")) {
                    log.info("Server console user activity");
                } else {
                    if (!allowedIdps.isEmpty() && !allowedIdps.contains(userIdp) && !allowedIdps.contains("*")) {
                        throw new ApplicationException(ExceptionEnum.ERR_1036.getErrorResponseBody("Insufficient permission for logged in user"), HttpStatus.FORBIDDEN);
                    }

                    if (!allowedRoles.isEmpty() && (userRoles == null || Collections.disjoint(allowedRoles, userRoles))) {
                        throw new ApplicationException(ExceptionEnum.ERR_1036.getErrorResponseBody("Insufficient permission for logged in user"), HttpStatus.FORBIDDEN);
                    }
                }

                Set<String> resolvedApiRole = new HashSet<>(allowedRoles);
                resolvedApiRole.retainAll(userRoles);

                String resolvedUserRole = resolvedApiRole.stream().findFirst().orElse("");
                String userType = claims.getClaimAsString("user-type");
                String tenant = claims.getClaimAsString("tenant");

                String userId = claims.getSubject();
                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .header("X-User-Tenant", tenant)
                        .header("X-User-Role", resolvedUserRole)
                        .header("X-User-Type", userType)
                        .header("X-User-Id", userId)
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
            } catch (ExpiredJWTException e) {
                log.error("JWT Token Expired: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } catch (BadJWTException e) {
                log.error("JWT claims are invalid: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } catch (BadJWSException e) {
                log.error("Invalid JWT token: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1037.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            } catch (ApplicationException e) {
                log.error("Failed to validate the request: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("Failed to validate the request: {}", e.getMessage(), e);
                throw new ApplicationException(ExceptionEnum.ERR_1037.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
            }
        };
    }
}