//package com.photon.bootstrap;
//
//import com.photon.dto.ApiResponseDto;
//import com.photon.endpoint.dto.EndpointDetailsDto;
//import com.photon.endpoint.dto.FeatureInfoDto;
//import com.photon.endpoint.service.ReactiveEndpointScannerService;
//import com.photon.properties.ApplicationConfigProperties;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.scheduler.Schedulers;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.Set;
//
//@Slf4j
//@Order(1)
//@Component
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//public class ReactiveEndpointRegistrationRunner implements ApplicationRunner {
//
//    private final ReactiveEndpointScannerService endpointScannerService;
//    private final ApplicationConfigProperties applicationConfigProperties;
//    private final WebClient.Builder webClientBuilder;
//
//    public ReactiveEndpointRegistrationRunner(ReactiveEndpointScannerService endpointScannerService,
//                                              ApplicationConfigProperties applicationConfigProperties,
//                                              @Qualifier("loadBalancedWebClient") WebClient.Builder loadBalancedWebClient) {
//        this.endpointScannerService = endpointScannerService;
//        this.applicationConfigProperties = applicationConfigProperties;
//        this.webClientBuilder = loadBalancedWebClient;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        endpointScannerService.scanEndpoints()
//                .map(response -> {
//                    Set<FeatureInfoDto> featureList = response.getResponseData().getFeatures();
//
//                    return EndpointDetailsDto.builder()
//                            .features(featureList)
//                            .id(applicationConfigProperties.getApplicationName())
//                            .name(applicationConfigProperties.getApplicationName())
//                            .clientId(applicationConfigProperties.getXApiKey())
//                            .clientSecret(applicationConfigProperties.getXApiSecret())
//                            .models(response.getResponseData().getModels())
//                            .build();
//                })
//                .flatMap(endpointDetailsDto -> {
//                    String username = applicationConfigProperties.getCompositeXApiKey();
//                    String password = applicationConfigProperties.getCompositeXApiSecret();
//                    String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
//
//                    String url = "lb://CONSOLE/api-manager/services/" + applicationConfigProperties.getApplicationName();
//
//                    return webClientBuilder.build()
//                            .put()
//                            .uri(url)
//                            .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .bodyValue(endpointDetailsDto)
//                            .retrieve()
//                            .bodyToMono(ApiResponseDto.class);
//                })
//                .subscribeOn(Schedulers.boundedElastic())
//                .subscribe(
//                        response -> log.warn("Register endpoints response: {}", response),
//                        error -> log.error("Error while registering endpoints", error)
//                );
//    }
//}