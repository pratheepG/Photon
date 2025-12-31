package com.photon.console.identity.repository;

import com.photon.console.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByRoleId(String rId);
    Optional<Role> findByRoleIdAndIdp(String rId, String idp);
}