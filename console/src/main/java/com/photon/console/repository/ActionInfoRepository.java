package com.photon.console.repository;

import com.photon.console.entity.ActionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActionInfoRepository extends JpaRepository<ActionInfo, UUID> {
    Optional<ActionInfo> findByActionId(String actionId);
    Optional<List<ActionInfo>> findByFeature_Id(UUID featureId);
}