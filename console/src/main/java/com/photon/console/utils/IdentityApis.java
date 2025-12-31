package com.photon.console.utils;

import com.photon.dto.ApiResponseDto;
import com.photon.dto.request.RoleFeatureActionRequestDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class IdentityApis {

    private final ApplicationConfigProperties applicationConfigProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public IdentityApis(ApplicationConfigProperties applicationConfigProperties, RestTemplate restTemplate) {
        this.applicationConfigProperties = applicationConfigProperties;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<ApiResponseDto<?>> addFeatureActionInRole(String roleId, Map<String, Set<String>> featureActions) throws ApplicationException {
        try {
            String url = String.format("lb://%s/role/%s/feature-action", "IDENTITY", roleId);

            HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(featureActions, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PATCH, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in patchRole : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in patchRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponseDto<?>> addAllFeatureActionInRole(List<RoleFeatureActionRequestDto> roleFeatureActionList) throws ApplicationException {
        try {
            String url = String.format("lb://%s/role/feature-action", "IDENTITY");

            HttpEntity<List<RoleFeatureActionRequestDto>> entity = new HttpEntity<>(roleFeatureActionList, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PATCH, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in addAllFeatureActionInRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in addAllFeatureActionInRole: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponseDto<?>> deleteAllFeatureActionInRole(List<RoleFeatureActionRequestDto> roleFeatureActionList) throws ApplicationException {
        try {
            String url = String.format("lb://%s/role/feature-action", "IDENTITY");

            HttpEntity<List<RoleFeatureActionRequestDto>> entity = new HttpEntity<>(roleFeatureActionList, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.DELETE, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteAllFeatureActionInRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteAllFeatureActionInRole: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponseDto<?>> deleteFeatureActionFromRole(String roleId, Map<String, Set<String>> featureActions) throws ApplicationException {
        try {
            String url = String.format("lb://%s/role/%s/feature-action", "IDENTITY", roleId);

            HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(featureActions, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PATCH, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteFeatureActionFromRole: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteFeatureActionFromRole : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders getHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String username = this.applicationConfigProperties.getXApiKey();
        String password = this.applicationConfigProperties.getXApiSecret();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);

        headers.set("Authorization", authHeader);
        return headers;
    }

}