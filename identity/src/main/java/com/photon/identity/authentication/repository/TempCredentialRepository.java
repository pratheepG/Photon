package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.commons.enums.AuthAdaptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempCredentialRepository extends JpaRepository<TempCredential, String> {
    Optional<TempCredential> findBySecurityKey(String securityCode);
    Optional<List<TempCredential>> findByAuthTypeAndAuthAdaptorAndOperationAndRequestIdAndDeviceIdAndCreatedDateAfter(String authType, AuthAdaptor authAdaptor, String operation, String requestId, String deviceId, Date date);

    @Modifying
    @Query("UPDATE TempCredential t SET t.isActive = false WHERE t.phoneNumber = :phoneNumber AND t.operation = :operation")
    void deactivateOldCredentials(@Param("phoneNumber") String phoneNumber, @Param("operation") String operation);
}