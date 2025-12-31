package com.photon.identity.authentication.repository;

import com.photon.endpoint.enums.AccessLevel;
import com.photon.identity.authentication.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByRoleId(String rId);
    Optional<Role> findByRoleIdAndIdp(String rId, String idp);
    Page<Role> findAll(Pageable pageable);
    Page<Role> findAllByAccessLevel(AccessLevel accessLevel, Pageable pageable);
    Page<Role> findAllByAccessLevelIn(List<AccessLevel> accessLevels, Pageable pageable);
}