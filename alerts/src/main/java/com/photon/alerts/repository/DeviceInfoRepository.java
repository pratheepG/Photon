package com.photon.alerts.repository;

import com.photon.alerts.entity.DeviceInfo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DeviceInfoRepository extends ReactiveCrudRepository<DeviceInfo, UUID> {
    Mono<DeviceInfo> findByDeviceId(String deviceId);
    Flux<DeviceInfo> findByIsActiveTrue();
}