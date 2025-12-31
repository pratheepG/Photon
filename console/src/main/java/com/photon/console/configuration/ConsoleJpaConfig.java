package com.photon.console.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = { "com.photon.console.repository", "com.photon.console.locality.repository"},
        entityManagerFactoryRef = "consoleEntityManagerFactory")
public class ConsoleJpaConfig {}