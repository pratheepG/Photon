package com.photon.apiconfig.repository;

import com.photon.apiconfig.entity.ConfigProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigPropertiesRepository extends JpaRepository<ConfigProperties, String> {
    Optional<ConfigProperties> findByIdAndProfile(String id, String profile);
}