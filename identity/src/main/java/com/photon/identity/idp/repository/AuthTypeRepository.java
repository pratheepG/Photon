package com.photon.identity.idp.repository;

import com.photon.identity.idp.entity.AuthType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AuthTypeRepository extends JpaRepository<AuthType, String>, JpaSpecificationExecutor<AuthType> {

    Page<AuthType> findAll(Pageable pageable);
    Optional<AuthType> findById(String id);
}