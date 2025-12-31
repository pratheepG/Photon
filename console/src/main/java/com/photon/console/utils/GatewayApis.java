package com.photon.console.utils;

import com.photon.console.dto.request.GatewayRoutRequestDto;
import com.photon.dto.ApiResponseDto;
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
import java.util.Base64;

@Slf4j
@Component
public class GatewayApis {

    private final ApplicationConfigProperties applicationConfigProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public GatewayApis(ApplicationConfigProperties applicationConfigProperties, RestTemplate restTemplate) {
        this.applicationConfigProperties = applicationConfigProperties;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<ApiResponseDto<?>> addOrUpdateEndpointPath(String routeId, GatewayRoutRequestDto gatewayRoutRequest) throws ApplicationException {
        try {
            String url = String.format("lb://%s/gateway/path/%s", "API-GATEWAY", routeId);

            HttpEntity<GatewayRoutRequestDto> entity = new HttpEntity<>(gatewayRoutRequest, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in addOrUpdateEndpointPath : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in addOrUpdateEndpointPath : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponseDto<?>> deleteEndpointPath(String routeId) throws ApplicationException {
        try {
            String url = String.format("lb://%s/gateway/path/%s", "API-GATEWAY", routeId);

            HttpEntity<Object> entity = new HttpEntity<>(this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.DELETE, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteEndpointPath : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in deleteEndpointPath : {} ",e.getMessage());
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