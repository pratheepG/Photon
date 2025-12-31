package com.photon.content.location.repository;

import com.photon.content.location.entity.LocalityImportStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LocalityImportStatusRepo extends ReactiveCrudRepository<LocalityImportStatus, Long> {
    Mono<LocalityImportStatus> findByJobName(String jobName);
}