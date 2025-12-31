package com.photon.apiconfig.configuration;

import com.photon.apiconfig.repository.CustomCompositeEnvironmentRepository;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigServerConfiguration {

    private final CustomCompositeEnvironmentRepository customCompositeEnvironmentRepository;

    public ConfigServerConfiguration(CustomCompositeEnvironmentRepository customCompositeEnvironmentRepository) {
        this.customCompositeEnvironmentRepository = customCompositeEnvironmentRepository;
    }

    @Bean
    public EnvironmentRepository environmentRepository() {
        return customCompositeEnvironmentRepository;
    }

//    @Bean
//    @LoadBalanced
//    public RestTemplate restTemplate() {
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        return new RestTemplate(factory);
//    }
}