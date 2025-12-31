package com.photon.alerts.repository;

import com.photon.alerts.entity.Alert;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AlertRepository extends ReactiveCrudRepository<Alert, Long> {
    Mono<Alert> findByAlertTypeAndAlertSubType(String alertType, String alertSubType);
}