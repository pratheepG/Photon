package com.photon.identity.idp.repository;

import com.photon.identity.idp.entity.MFAConditionSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MFAConditionRepository extends JpaRepository<MFAConditionSet, Long> {
}