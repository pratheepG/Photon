package com.photon.content.location.repository;

import com.photon.content.location.entity.District;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DistrictRepository extends ReactiveCrudRepository<District, Long> {
    Mono<District> findByStateIdAndName(Long stateId, String name);
    Flux<District> findByStateId(Long stateId);
    Flux<District> findByNameContainingIgnoreCase(String name);
    Flux<District> findByNameContainingIgnoreCaseAndStateId(String name, Long stateId);
    Flux<District> findAllByOrderByNameAsc();
}