package com.photon.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.photon.constants.ApplicationConstants;
import com.photon.dto.JwtHeaderDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Reactive utility class for accessing HTTP request headers in a non-blocking manner.
 * This class is designed to be used within a Spring WebFlux application.
 * It uses static methods to retrieve the ServerHttpRequest indirectly from the reactive context.
 */
@Slf4j
public class ReactiveHttpRequestManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Mono<ReportingParamsDto> getReportingParams(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(ApplicationConstants.REPORTING_PARAMS);
        if (header == null) {
            return Mono.empty();
        }
        try {
            ReportingParamsDto dto = new Gson().fromJson(StringEscapeUtils.unescapeJson(header), ReportingParamsDto.class);
            return Mono.just(dto);
        } catch (Exception e) {
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1028.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
        }
    }

    public static Mono<String> getUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(ApplicationConstants.X_USER_ID);
        return userId != null ?
                Mono.just(userId)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getUserTenant(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(ApplicationConstants.X_USER_TENANT);
        return userId != null ?
                Mono.just(userId)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getUserRole(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(ApplicationConstants.X_USER_ROLE);
        return userId != null ?
                Mono.just(userId)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getIdp(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst(ApplicationConstants.X_IDENTITY_PROVIDER);
        return userId != null ?
                Mono.just(userId)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getUserType(ServerHttpRequest request) {
        String userType = request.getHeaders().getFirst(ApplicationConstants.X_USER_TYPE);
        return userType != null ?
                Mono.just(userType)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getTokenId(ServerHttpRequest request) {
        String jti = request.getHeaders().getFirst(ApplicationConstants.X_TOKEN_ID);
        return jti != null ?
                Mono.just(jti)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getTokenExpiry(ServerHttpRequest request) {
        String jti = request.getHeaders().getFirst(ApplicationConstants.X_TOKEN_EXPIRY);
        return jti != null ?
                Mono.just(jti)
                : Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
    }

    public static Mono<String> getAuthorizationToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(ApplicationConstants.AUTHORIZATION);
        if (token == null || token.isBlank()) {
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
        }
        return Mono.just(token);
    }

    public static Mono<String> getUserIdFromContext() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ApplicationConstants.X_USER_ID)) {
                return Mono.justOrEmpty(ctx.get(ApplicationConstants.X_USER_ID));
            }
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
        });
    }

    public static Mono<String> getUserTypeFromContext() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ApplicationConstants.X_USER_TYPE)) {
                return Mono.justOrEmpty(ctx.get(ApplicationConstants.X_USER_TYPE));
            }
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
        });
    }

    public static Mono<String> getAuthorizationTokenFromContext() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ApplicationConstants.AUTHORIZATION)) {
                String token = ctx.get(ApplicationConstants.AUTHORIZATION);
                if (token.isBlank()) {
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
                }
                return Mono.just(token);
            }
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED));
        });
    }

    public static Mono<JwtHeaderDto> readJwtHeader(String jwtToken) {
        try {
            if (jwtToken == null || jwtToken.isBlank()) {
                return Mono.error(new IllegalArgumentException("JWT token is missing!"));
            }
            jwtToken = jwtToken.trim();
            if (jwtToken.toLowerCase().startsWith("bearer ")) {
                jwtToken = jwtToken.substring(7).trim();
            }
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return Mono.error(new IllegalArgumentException("Invalid JWT token!"));
            }
            String headerPart = parts[0].replaceAll("\\s+", "");
            byte[] decodedBytes;
            try {
                decodedBytes = Base64.getUrlDecoder().decode(headerPart);
            } catch (IllegalArgumentException e) {
                return Mono.error(new IllegalArgumentException("JWT header is not valid Base64Url: " + headerPart, e));
            }
            String headerJson = new String(decodedBytes, StandardCharsets.UTF_8);
            log.info("Header: " + headerJson);
            JwtHeaderDto dto = objectMapper.readValue(headerJson, JwtHeaderDto.class);
            return Mono.just(dto);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}