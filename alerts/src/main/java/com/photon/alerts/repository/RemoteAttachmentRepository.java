package com.photon.alerts.repository;

import com.photon.alerts.entity.RemoteAttachment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RemoteAttachmentRepository extends ReactiveCrudRepository<RemoteAttachment, Long> {
    Flux<RemoteAttachment> findByTemplateId(Long  templateId);
}