package com.photon.alerts.repository;

import com.photon.alerts.entity.SubscriberTopicMap;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SubscriberTopicMapRepository extends CrudRepository<SubscriberTopicMap, UUID> {
    Flux<SubscriberTopicMap> findSubscriberTopicMapByTopic(String topic);
}