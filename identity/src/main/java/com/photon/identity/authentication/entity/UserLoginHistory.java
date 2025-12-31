package com.photon.identity.authentication.entity;

import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.LoginFactor;
import com.photon.identity.commons.enums.LoginStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_login_history")
public class UserLoginHistory {

    @Id
    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "login_principle", nullable = false)
    private String loginPrinciple;

    @Column(name = "auth_type", nullable = false, length = 50)
    private String authType;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_adaptor", nullable = false, length = 50)
    private AuthAdaptor authAdaptor;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_factor", nullable = false, length = 10)
    private LoginFactor loginFactor;

    @Builder.Default
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess = true;

    @Builder.Default
    @Column(name = "is_retry", nullable = false)
    private Boolean isRetry = true;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "device_id", length = 512)
    private String deviceId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "login_timestamp", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 50)
    private LoginStatus loginStatus;

    @Column(name = "created_on")
    private Date createdOn;
}