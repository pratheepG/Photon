package com.photon.console.gateway.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.photon.console.gateway.repository", entityManagerFactoryRef = "gatewayEntityManagerFactory")
public class GatewayJpaConfig {}
