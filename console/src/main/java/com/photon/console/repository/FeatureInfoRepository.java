package com.photon.console.repository;

import com.photon.console.entity.FeatureInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureInfoRepository extends JpaRepository<FeatureInfo, UUID> {
    Optional<FeatureInfo> findByFeatureId(String featureId);
    void deleteByFeatureId(String featureId);
    Page<FeatureInfo> findByEndpointDetails_Id(Pageable pageable, String endpointId);
}