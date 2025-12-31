package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.entity.UserLoginHistory;
import com.photon.identity.commons.enums.LoginStatus;
import com.photon.identity.commons.enums.LoginFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, UUID> {
    Optional<UserLoginHistory> findTopByUserIdAndLoginFactorAndIsSuccessOrderByLoginTimestampDesc(String userId, LoginFactor loginFactor, Boolean isSuccess);
    List<UserLoginHistory> findByUserIdAndRequestId(String userId, String requestId);
    Optional<UserLoginHistory> findByRequestId(String requestId);
    Optional<UserLoginHistory> findByRequestIdAndAuthTypeAndUserId(String sessionId, String authType, String userId);
    Optional<List<UserLoginHistory>> findByUserIdAndRequestIdAndLoginStatusAndLoginFactorAndIsSuccessAndLoginTimestampAfter(String userId, String requestId, LoginStatus loginStatus, LoginFactor loginFactor, Boolean isSuccess, Date createdAt);
    Optional<List<UserLoginHistory>> findByUserIdAndLoginStatusAndLoginTimestampBefore(String userId, LoginStatus loginStep, Date createdAt);
}