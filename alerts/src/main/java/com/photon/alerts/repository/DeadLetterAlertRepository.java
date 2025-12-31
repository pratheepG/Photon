package com.photon.alerts.repository;

import com.photon.alerts.entity.DeadLetterAlert;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DeadLetterAlertRepository extends ReactiveCrudRepository<DeadLetterAlert, Long> {
    Flux<DeadLetterAlert> findAllByManuallyProcessedFalse();
}