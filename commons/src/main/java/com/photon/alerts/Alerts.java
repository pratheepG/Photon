package com.photon.alerts;

import com.photon.alerts.dto.AlertEventDto;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.properties.ApplicationConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class Alerts {

    private static final String APPLICATION_ID = "ALERTS";
    private static final String INSTANT_SMS_ENDPOINT = "user-alerts/send";
    private static final String QUEUE_SMS_ENDPOINT = "async/sms";
    private final RestTemplate restTemplate;
    private final ApplicationConfigProperties applicationConfigProperties;

    public Alerts(RestTemplate restTemplate, ApplicationConfigProperties applicationConfigProperties) {
        this.restTemplate = restTemplate;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    public ResponseEntity<ApiResponseDto<?>> sendSmsInstantlyForSubscriber(AlertEventDto alertDto) throws ApplicationException {
        try {
            String url = String.format("lb://%s/%s", APPLICATION_ID, INSTANT_SMS_ENDPOINT);

            HttpEntity<AlertEventDto> entity = new HttpEntity<>(alertDto, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in sendInstantlyForSubscriber : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in sendInstantlyForSubscriber : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ApiResponseDto<?>> addSmsToQueueForSubscriber(AlertEventDto alertDto) throws ApplicationException {
        try {
            String url = String.format("lb://%s/%s", APPLICATION_ID, QUEUE_SMS_ENDPOINT);

            HttpEntity<AlertEventDto> entity = new HttpEntity<>(alertDto, this.getHeader());
            return this.restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<>() {});
        } catch (ApplicationException ae) {
            log.error("ApplicationException in addSmsToQueueForSubscriber : {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in addSmsToQueueForSubscriber : {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders getHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String username = this.applicationConfigProperties.getCompositeXApiKey();
        String password = this.applicationConfigProperties.getCompositeXApiSecret();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);

        headers.set("Authorization", authHeader);
        return headers;
    }
}