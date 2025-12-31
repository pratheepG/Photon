package com.photon.alerts.repository;

import com.photon.alerts.entity.SubscriberChannelPreference;
import com.photon.alerts.enums.Channel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SubscriberChannelPreferenceRepository extends ReactiveCrudRepository<SubscriberChannelPreference, UUID> {
    Flux<SubscriberChannelPreference> findBySubscriberId(UUID subscriberId);
    Mono<Void> deleteBySubscriberId(UUID subscriberId);
    Mono<SubscriberChannelPreference> findBySubscriberIdAndChannel(UUID subscriberId, Channel channel);
}