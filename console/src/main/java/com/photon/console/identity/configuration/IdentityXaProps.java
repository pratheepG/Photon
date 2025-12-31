package com.photon.console.identity.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.datasource.identity")
public record IdentityXaProps(String url, String username, String password) {}