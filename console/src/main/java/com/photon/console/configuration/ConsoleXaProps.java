package com.photon.console.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.datasource.console")
public record ConsoleXaProps(String url, String username, String password) {}