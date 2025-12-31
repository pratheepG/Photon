package com.photon.storage.repository;

import com.photon.storage.entity.FileMetadata;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileMetadataRepository extends ReactiveCrudRepository<FileMetadata, UUID> {
}