package com.photon.identity.commons.repository;

import com.photon.identity.commons.entity.CdnAssetInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdnAssetInfoRepository extends JpaRepository<CdnAssetInfo, Long> {
}