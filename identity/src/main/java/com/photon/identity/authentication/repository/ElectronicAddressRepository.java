package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.entity.ElectronicAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ElectronicAddressRepository extends JpaRepository<ElectronicAddress, Long> {
    Optional<ElectronicAddress> findByValue(String value);
}