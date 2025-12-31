package com.photon.identity.idp.repository;

import com.photon.identity.commons.enums.IdentityProviderType;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.entity.IdentityProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface IdentityProviderRepository extends JpaRepository<IdentityProvider,String> {

    @Query("SELECT new com.photon.identity.idp.dto.IdentityProviderDto(ip.id, ip.name, ip.description, ip.isActive) " + "FROM IdentityProvider ip")
    Page<IdentityProviderDto> findAllIdentityProviders(Pageable pageable);

    @Query("SELECT new com.photon.identity.idp.dto.IdentityProviderDto(ip.id, ip.name, ip.description, ip.isActive) " + "FROM IdentityProvider ip where ip.identityProviderType = :type")
    Page<IdentityProviderDto> findAllOnboardingIdentityProviders(Pageable pageable, @Param("type") IdentityProviderType type);
}