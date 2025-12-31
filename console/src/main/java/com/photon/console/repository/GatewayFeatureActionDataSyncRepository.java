package com.photon.console.repository;

import com.photon.console.entity.GatewayFeatureActionDataSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GatewayFeatureActionDataSyncRepository extends JpaRepository<GatewayFeatureActionDataSync, UUID> {
}