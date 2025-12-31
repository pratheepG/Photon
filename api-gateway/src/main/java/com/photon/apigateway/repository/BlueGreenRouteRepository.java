package com.photon.apigateway.repository;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class BlueGreenRouteRepository implements RouteDefinitionRepository {

    private final AtomicReference<Map<String, RouteDefinition>> activeRoutes = new AtomicReference<>(new ConcurrentHashMap<>());
    private final Map<String, RouteDefinition> standbyRoutes = new ConcurrentHashMap<>();

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(activeRoutes.get().values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.doOnNext(rd -> standbyRoutes.put(rd.getId(), rd)).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.doOnNext(standbyRoutes::remove).then();
    }

    // 🔄 Switch standby into active
    public void promote() {
        activeRoutes.set(new ConcurrentHashMap<>(standbyRoutes));
        standbyRoutes.clear();
    }
}