package com.photon.alerts.repository;

import com.photon.alerts.entity.AlertTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AlertTemplateRepository extends ReactiveCrudRepository<AlertTemplate, Long> {
    Flux<AlertTemplate> findByAlertId(Long alertId);
}