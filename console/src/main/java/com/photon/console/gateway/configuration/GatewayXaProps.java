package com.photon.console.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.datasource.gateway")
public record GatewayXaProps(String url, String username, String password) {}