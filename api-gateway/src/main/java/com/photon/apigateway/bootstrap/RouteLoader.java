package com.photon.apigateway.bootstrap;

import com.photon.apigateway.service.GatewayRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RouteLoader implements CommandLineRunner {

    @Autowired
    private GatewayRouteService gatewayRouteService;

    @Override
    public void run(String... args) {
        gatewayRouteService.loadRoutesFromDB()
                .then()
                .doOnSuccess(unused -> log.info("Loaded routes from database"))
                .doOnError(throwable -> log.error("Error loading routes from database", throwable))
                .subscribe();
    }
}