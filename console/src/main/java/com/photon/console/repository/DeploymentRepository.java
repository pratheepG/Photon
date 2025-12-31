package com.photon.console.repository;

import com.photon.console.dto.response.DeploymentArtifactDetailsResponseDto;
import com.photon.console.dto.response.DeploymentArtifactResponseDto;
import com.photon.console.entity.DeploymentArtifact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<DeploymentArtifact, UUID> {
    @Query("SELECT new com.photon.console.dto.response.DeploymentArtifactResponseDto(d.id, d.serviceName, d.jarFileName, d.deployed) FROM DeploymentArtifact d")
    Page<DeploymentArtifactResponseDto> findAllDtos(Pageable pageable);
    Page<DeploymentArtifact> findAll(Pageable page);

    @Query("SELECT new com.photon.console.dto.response.DeploymentArtifactDetailsResponseDto(" +
            "d.id, d.serviceName, d.jarFileName, d.dockerfileContent, " +
            "d.environment, d.appId, d.appSecret, d.deployed) " +
            "FROM DeploymentArtifact d WHERE d.id = :id")
    Optional<DeploymentArtifactDetailsResponseDto> findDetailsById(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE DeploymentArtifact d SET d.dockerfileContent = :dockerfileContent WHERE d.id = :id")
    int updateDockerfileContentById(@Param("id") UUID id, @Param("dockerfileContent") String dockerfileContent);
}