package com.photon.identity.idp.repository;

import com.photon.identity.idp.entity.SCAConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SCAConfigurationRepository extends JpaRepository<SCAConfiguration, Long> {

    Page<SCAConfiguration> findAll(Pageable pageable);

}