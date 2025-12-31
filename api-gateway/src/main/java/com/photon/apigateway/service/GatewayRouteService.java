package com.photon.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.apigateway.cache.SelectiveCachingRouteLocator;
import com.photon.apigateway.dto.GatewayRoutRequestDto;
import com.photon.apigateway.entity.GatewayRoute;
import com.photon.apigateway.repository.GatewayRouteRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.SuccessEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
public class GatewayRouteService {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final SelectiveCachingRouteLocator selectiveLocator;
    private final GatewayRouteRepository gatewayRouteRepository;
    private final InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository;

    public GatewayRouteService(GatewayRouteRepository gatewayRouteRepository, InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository, ApplicationEventPublisher publisher, SelectiveCachingRouteLocator selectiveLocator) {
        this.gatewayRouteRepository = gatewayRouteRepository;
        this.inMemoryRouteDefinitionRepository = inMemoryRouteDefinitionRepository;
        this.selectiveLocator = selectiveLocator;
        this.objectMapper = new ObjectMapper();
        this.publisher = publisher;
    }

    public Mono<Void> saveRoute(RouteDefinition routeDefinition) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(routeDefinition))
                .flatMap(routeJson -> {
                    GatewayRoute gatewayRoute = new GatewayRoute();
                    gatewayRoute.setRouteId(routeDefinition.getId());
                    gatewayRoute.setRouteDefinition(routeJson);
                    return gatewayRouteRepository.save(gatewayRoute);
                })
                .then(inMemoryRouteDefinitionRepository.save(Mono.just(routeDefinition)))
                .then(Mono.defer(() -> {
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.empty();
                }));
    }

    public Mono<ApiResponseDto<?>> updatePath(String routeId, GatewayRoutRequestDto dto) {
        RouteDefinition rd = dto.getRouteDefinition();
        return gatewayRouteRepository.deleteByRouteId(routeId)
                .then(Mono.fromCallable(dto::toEntity).flatMap(gatewayRouteRepository::save))
                .then(inMemoryRouteDefinitionRepository.delete(Mono.just(routeId)))
                .then(inMemoryRouteDefinitionRepository.save(Mono.just(rd)))
                .then(selectiveLocator.refreshOne(routeId))
                .thenReturn(SuccessEnum.UPDATED.getSuccessResponseBody());
    }

    public Mono<ApiResponseDto<?>> deletePath(String routeId) {
        return gatewayRouteRepository.deleteByRouteId(routeId)
                .then(inMemoryRouteDefinitionRepository.delete(Mono.just(routeId)))
                .then(selectiveLocator.evict(routeId))
                .thenReturn(SuccessEnum.DELETED.getSuccessResponseBody());
    }

    public Mono<ApiResponseDto<?>> addOrUpdatePath(String routeId, GatewayRoutRequestDto gatewayRoutRequestDto) {
        log.debug("addPath routeId: {}, path: {}", routeId, gatewayRoutRequestDto.getPath());
        return getRouteById(routeId)
                .<ApiResponseDto<?>>flatMap(routeDefinition -> {
                    log.debug("Route found: {}", routeId);
                    return Mono.fromCallable(gatewayRoutRequestDto::toEntity)
                            .flatMap(gatewayRoute -> gatewayRouteRepository.deleteByRouteId(routeId).then(Mono.just(gatewayRoute)))
                            .flatMap(gatewayRoute -> gatewayRouteRepository.save(gatewayRoute).then(Mono.just(gatewayRoute)))
                            .flatMap(savedRoute -> inMemoryRouteDefinitionRepository.delete(Mono.just(routeId)).then(Mono.just(savedRoute)))
                            .flatMap(gatewayRoute -> inMemoryRouteDefinitionRepository.save(Mono.just(gatewayRoutRequestDto.getRouteDefinition())))
                            .thenReturn((ApiResponseDto<?>) SuccessEnum.UPDATED.getSuccessResponseBody());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Route not found, creating new route with ID: {}", routeId);
                    return Mono.fromCallable(gatewayRoutRequestDto::toEntity)
                            .flatMap(gatewayRouteRepository::save)
                            .flatMap(gatewayRoute -> inMemoryRouteDefinitionRepository.save(Mono.just(gatewayRoutRequestDto.getRouteDefinition())))
                            .thenReturn((ApiResponseDto<?>) SuccessEnum.CREATED.getSuccessResponseBody());
                }))
                .doOnSuccess(aVoid -> selectiveLocator.refreshOne(routeId))
                .onErrorResume(e -> {
                    log.error("Error while adding route: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to add path to route", e));
                });
    }

    public Mono<Void> deleteRoute(String routeId) {
        return gatewayRouteRepository.deleteByRouteId(routeId)
                .then(inMemoryRouteDefinitionRepository.delete(Mono.just(routeId)))
                .onErrorResume(NotFoundException.class, ex -> Mono.empty())
                .then(Mono.defer(() -> {
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.empty();
                }));
    }

    public Mono<RouteDefinition> getRouteById(String id) {
        return gatewayRouteRepository.findByRouteId(id)
                .flatMap(gatewayRoute -> {
                    try {
                        RouteDefinition routeDefinition = objectMapper.readValue(gatewayRoute.getRouteDefinition(), RouteDefinition.class);
                        return Mono.just(routeDefinition);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to deserialize route definition", e));
                    }
                });
    }

    public Flux<RouteDefinition> loadRoutesFromDB() {
        log.info("Loading routes from database");
        return gatewayRouteRepository.findAll()
                .flatMap(gatewayRoute -> {
                    try {
                        RouteDefinition routeDefinition = objectMapper.readValue(gatewayRoute.getRouteDefinition(), RouteDefinition.class);
                        return inMemoryRouteDefinitionRepository.save(Mono.just(routeDefinition)).thenReturn(routeDefinition);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to deserialize route definition", e));
                    }
                })
                .collectList()
                .flatMapMany(routeDefinitions -> {
                    this.selectiveLocator.refreshAll();
                    return Flux.fromIterable(routeDefinitions);
                });
    }

    public Mono<Void> updateRoute(String routeId, RouteDefinition newRouteDefinition) {
        return deleteRoute(routeId)
                .then(saveRoute(newRouteDefinition))
                .then(Mono.defer(() -> {
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                    return Mono.empty();
                }));
    }

    public Mono<Void> refreshAddedRoute(String routeId) {
        return gatewayRouteRepository.findByRouteId(routeId)
                .flatMap(route -> {
                    try {
                        RouteDefinition rd = objectMapper.readValue(route.getRouteDefinition(), RouteDefinition.class);
                        return inMemoryRouteDefinitionRepository.save(Mono.just(rd))
                                .then(selectiveLocator.refreshOne(routeId));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Failed to deserialize route definition", e));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error while add/update path for {}: {}", routeId, e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to add/update path", e));
                });
    }

    public Mono<Void> refreshRemovedRoute(String routeId) {
        return inMemoryRouteDefinitionRepository.delete(Mono.just(routeId))
                .then(selectiveLocator.refreshOne(routeId))
                .onErrorResume(e -> {
                    log.error("Error while add/update path for {}: {}", routeId, e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to add/update path", e));
                });
    }

    public Mono<Void> refreshAllRoute() {
        log.info("Loading all routes from database");
        return gatewayRouteRepository.findAll()
                .concatMap(gatewayRoute -> {
                    try {
                        RouteDefinition rd = objectMapper.readValue(gatewayRoute.getRouteDefinition(), RouteDefinition.class);
                        return inMemoryRouteDefinitionRepository.save(Mono.just(rd));
                    } catch (Exception e) {
                        log.error("Skipping bad route: {}", gatewayRoute.getRouteId(), e);
                        return Mono.empty();
                    }
                })
                .then(selectiveLocator.refreshAll());
    }

}