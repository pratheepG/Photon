package com.photon.apiconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableConfigServer
@SpringBootApplication
@ComponentScan(basePackages = {"com.photon.exception", "com.photon.config", "com.photon.apiconfig"})
public class ApiConfigApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApiConfigApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApiConfigApplication.class);
    }

}