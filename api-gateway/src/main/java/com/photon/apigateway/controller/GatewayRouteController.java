package com.photon.apigateway.controller;

import com.photon.apigateway.dto.GatewayRoutRequestDto;
import com.photon.apigateway.service.GatewayRouteService;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.SuccessEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayRouteController {

    private final GatewayRouteService gatewayRouteService;
    private final RouteLocator routeLocator;

    @Autowired
    public GatewayRouteController(GatewayRouteService gatewayRouteService, RouteLocator routeLocator) {
        this.gatewayRouteService = gatewayRouteService;
        this.routeLocator = routeLocator;
    }


    @GetMapping("/api/routes")
    public Flux<Route> getAllRoutes() {
        return routeLocator.getRoutes();
    }

    @PostMapping("/routes")
    public Mono<ResponseEntity<String>> addRoute(@RequestBody RouteDefinition routeDefinition) {
        return gatewayRouteService.saveRoute(routeDefinition)
                .then(Mono.just(ResponseEntity.ok("Route added/updated successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Failed to add/update route")));
    }

    @PutMapping("/path/{id}")
    public Mono<ResponseEntity<ApiResponseDto<?>>> addPath(@PathVariable String id, @RequestBody GatewayRoutRequestDto gatewayRoutRequestDto) {
        gatewayRouteService.addOrUpdatePath(id, gatewayRoutRequestDto).subscribe();
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body(SuccessEnum.SUCCESS.getSuccessResponseBody()));
    }

    @DeleteMapping("/path/{id}")
    public Mono<ResponseEntity<ApiResponseDto<?>>> deletePath(@PathVariable String id) {
        gatewayRouteService.deletePath(id).subscribe();
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body(SuccessEnum.SUCCESS.getSuccessResponseBody()));
    }

    @DeleteMapping("/routes/{id}")
    public Mono<ResponseEntity<String>> deleteRoute(@PathVariable String id) {
        return gatewayRouteService.deleteRoute(id)
                .then(Mono.just(ResponseEntity.ok("Route deleted successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Failed to delete route")));
    }

    @PutMapping("/routes/{id}")
    public Mono<ResponseEntity<String>> updateRoute(@PathVariable String id, @RequestBody RouteDefinition routeDefinition) {
        return gatewayRouteService.updateRoute(id, routeDefinition)
                .then(Mono.just(ResponseEntity.ok("Route updated successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Failed to update route")));
    }

    @GetMapping(value = "/routes/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<RouteDefinition>> getRouteById(@PathVariable String id) {
        return gatewayRouteService.getRouteById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.status(404).body(null)));
    }

    @PostMapping("/routes/load")
    public Mono<ResponseEntity<String>> loadRoutes() {
        return gatewayRouteService.loadRoutesFromDB()
                .then(Mono.just(ResponseEntity.ok("Routes loaded from DB into in-memory cache")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Failed to load routes from DB")));
    }
}