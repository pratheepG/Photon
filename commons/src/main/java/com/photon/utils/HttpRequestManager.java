package com.photon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.photon.constants.ApplicationConstants;
import com.photon.dto.JwtHeaderDto;
import com.photon.dto.ReportingParamsDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class HttpRequestManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public static ReportingParamsDto getReportingParams() {
        try {
            return new Gson().fromJson(StringEscapeUtils.unescapeJson(Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.REPORTING_PARAMS)), ReportingParamsDto.class);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1028.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getTokenId() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_TOKEN_ID);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getTokenExpiry() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_TOKEN_EXPIRY);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getUserId() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_USER_ID);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getIdp() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_IDENTITY_PROVIDER);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getUserType() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_USER_TYPE);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getUserTenant() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_USER_TENANT);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getUserRole() {
        try {
            return Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.X_USER_ROLE);
        } catch (Exception e){
            throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public static String getAuthorizationToken() {
        String token = Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getHeader(ApplicationConstants.AUTHORIZATION);
        if (token == null || token.isBlank()) {
            throw new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
        }
        return token;
    }

    public static JwtHeaderDto readJwtHeader(String jwtToken) throws JsonProcessingException {
        if (jwtToken == null || jwtToken.isBlank()) {
            throw new IllegalArgumentException("JWT token is missing!");
        }

        jwtToken = jwtToken.trim();
        if (jwtToken.toLowerCase().startsWith("bearer ")) {
            jwtToken = jwtToken.substring(7).trim();
        }
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token!");
        }

        String headerPart = parts[0].replaceAll("\\s+", "");
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getUrlDecoder().decode(headerPart);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT header is not valid Base64Url: " + headerPart, e);
        }
        String headerJson = new String(decodedBytes, StandardCharsets.UTF_8);
        log.info("Header: " + headerJson);

        return objectMapper.readValue(headerJson, JwtHeaderDto.class);
    }


}