package com.photon.console.identity.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.photon.console.identity.repository", entityManagerFactoryRef = "identityEntityManagerFactory")
public class IdentityJpaConfig {}