//package com.photon.bootstrap;
//
//import com.photon.dto.ApiResponseDto;
//import com.photon.endpoint.dto.EndpointDetailsDto;
//import com.photon.endpoint.service.EndpointScannerService;
//import com.photon.properties.ApplicationConfigProperties;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.*;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.Objects;
//
//@Slf4j
//@Order(1)
//@Component
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//public class EndpointRegistrationRunner implements ApplicationRunner {
//
//    private final EndpointScannerService endpointScannerService;
//    private final ApplicationConfigProperties applicationConfigProperties;
//    private final RestTemplate restTemplate;
//
//    public EndpointRegistrationRunner(EndpointScannerService endpointScannerService, ApplicationConfigProperties applicationConfigProperties, RestTemplate restTemplate) {
//        this.endpointScannerService = endpointScannerService;
//        this.applicationConfigProperties = applicationConfigProperties;
//        this.restTemplate = restTemplate;
//    }
//
//    @Async
//    @Override
//    public void run(ApplicationArguments args) {
//        try {
//            EndpointDetailsDto endpointDetails = this.endpointScannerService.scanEndpoints().getResponseData();
//
//            String url = "lb://CONSOLE/api-manager/services/" + this.applicationConfigProperties.getApplicationName();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            String username = this.applicationConfigProperties.getCompositeXApiKey();
//            String password = this.applicationConfigProperties.getCompositeXApiSecret();
//            String auth = username + ":" + password;
//            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
//            String authHeader = "Basic " + new String(encodedAuth);
//
//            headers.set("Authorization", authHeader);
//
//            HttpEntity<EndpointDetailsDto> entity = new HttpEntity<>(endpointDetails, headers);
//            ResponseEntity<ApiResponseDto<?>> response = this.restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<>() {});
//            log.warn("Register endpoints response : {} ", Objects.requireNonNull(response.getBody()));
//        } catch (Exception e) {
//            log.error("Error while registering endpoints", e);
//        }
//    }
//}