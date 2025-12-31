package com.photon.config;

import com.photon.properties.AlertEventConfigProperties;
import com.photon.properties.ApplicationConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Order(1)
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class PhotonApplicationConfig {

    @Bean
    public ApplicationConfigProperties getApplicationConfigProperties() {
        return new ApplicationConfigProperties();
    }

    @Bean
    public AlertEventConfigProperties getAlertEventConfigProperties(Environment environment) {
        return new AlertEventConfigProperties(environment, getApplicationConfigProperties());
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

}