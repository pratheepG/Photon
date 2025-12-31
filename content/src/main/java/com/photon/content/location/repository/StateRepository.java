package com.photon.content.location.repository;

import com.photon.content.location.entity.State;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StateRepository extends ReactiveCrudRepository<State, Long> {
    Mono<Long> count();
    Mono<Boolean> existsByCode(String code);
    Mono<State> findByName(String name);
    Flux<State> findByNameContainingIgnoreCase(String name);
    Mono<State> findByCodeIgnoreCase(String code);
    Flux<State> findAllByOrderByNameAsc();
}