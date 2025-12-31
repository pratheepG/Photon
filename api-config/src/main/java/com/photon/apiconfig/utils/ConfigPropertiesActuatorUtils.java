package com.photon.apiconfig.utils;

import com.photon.properties.ApplicationConfigProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ConfigPropertiesActuatorUtils {

    private final RestTemplate restTemplate;
    private final ApplicationConfigProperties applicationConfigProperties;

    private static final String LOGGING_POSTFIX = "-LOGGING";
    private static final String SETTINGS_POSTFIX = "-SETTINGS";
    private static final String API_CONFIG_POSTFIX = "-API-CONFIG";
    private static final String API_CONFIG_IDENTITY_ONBOARDING_POSTFIX = "IDENTITY-ONBOARDING-CONFIG";

    public ConfigPropertiesActuatorUtils(RestTemplate restTemplate, ApplicationConfigProperties applicationConfigProperties) {
        this.restTemplate = restTemplate;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    @Async
    public void refreshConfig(String applicationId) {

        if(applicationId.contains(LOGGING_POSTFIX))
            applicationId = applicationId.replaceAll(LOGGING_POSTFIX, "");
        if(applicationId.contains(SETTINGS_POSTFIX))
            applicationId = applicationId.replaceAll(SETTINGS_POSTFIX, "");
        if(applicationId.contains(API_CONFIG_POSTFIX))
            applicationId = applicationId.replaceAll(API_CONFIG_POSTFIX, "");
        if(applicationId.contains(API_CONFIG_IDENTITY_ONBOARDING_POSTFIX))
            applicationId = applicationId.replaceAll(API_CONFIG_IDENTITY_ONBOARDING_POSTFIX, "");

        String url = UriComponentsBuilder.fromUriString("lb://"+applicationId+"/actuator/refresh").toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String username = this.applicationConfigProperties.getCompositeXApiKey();
        String password = this.applicationConfigProperties.getCompositeXApiSecret();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);

        headers.set("Authorization", authHeader);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<?> response = this.restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity, new ParameterizedTypeReference<>() {});
    }

}