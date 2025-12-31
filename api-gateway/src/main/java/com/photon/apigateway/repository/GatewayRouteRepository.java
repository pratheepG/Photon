package com.photon.apigateway.repository;

import com.photon.apigateway.entity.GatewayRoute;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface GatewayRouteRepository extends ReactiveCrudRepository<GatewayRoute, Long> {

    Mono<GatewayRoute> findByRouteId(String rId);
    Mono<Void> deleteByRouteId(String rId);
}