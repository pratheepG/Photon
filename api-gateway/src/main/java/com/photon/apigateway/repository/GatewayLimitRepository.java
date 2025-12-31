package com.photon.apigateway.repository;

import com.photon.apigateway.entity.GatewayLimit;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface GatewayLimitRepository extends R2dbcRepository<GatewayLimit, Long> {
  Mono<GatewayLimit> findByKeyName(String keyName);
}