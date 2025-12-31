package com.photon.content.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.photon.content.location.dto.response.StateResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Cache<String, List<StateResponse>> stateListCache() {
        return Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(Duration.ofHours(6))
                .build();
    }

    @Bean
    public Cache<String, StateResponse> stateByCodeCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofHours(12))
                .build();
    }

    @Bean
    public Cache<Object, Object> genericCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .build();
    }

}