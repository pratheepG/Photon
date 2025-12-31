package com.photon.alerts.repository;

import com.photon.alerts.entity.SubscriberDeviceMap;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscriberDeviceMapRepository extends ReactiveCrudRepository<SubscriberDeviceMap, UUID> {
}