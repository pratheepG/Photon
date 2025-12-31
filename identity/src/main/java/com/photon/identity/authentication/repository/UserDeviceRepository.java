package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.entity.RefreshToken;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);
    Optional<UserDevice> findByDeviceId(String deviceId);
    Optional<UserDevice> findByUserId(String userId);
}