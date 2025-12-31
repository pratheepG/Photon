package com.photon.alerts.repository;

import com.photon.alerts.entity.Subscriber;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface SubscriberRepository extends ReactiveCrudRepository<Subscriber, UUID> {
    Mono<Subscriber> findByUserId(String userId);
    Mono<Subscriber> findByUserName(String userName);
    Mono<Subscriber> findByUniqueId(String uniqueId);
    Flux<Subscriber> findSubscribersByIdIn(Collection<UUID> ids);
}