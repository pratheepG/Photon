package com.photon.console.repository;

import com.photon.console.entity.SdkArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SdkArtifactRepository extends JpaRepository<SdkArtifact, UUID> {
    Optional<SdkArtifact> findByModuleIdAndLanguageAndMetadataHash(String moduleId, String language, String metadataHash);
}