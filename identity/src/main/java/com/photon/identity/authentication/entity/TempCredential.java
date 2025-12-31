package com.photon.identity.authentication.entity;

import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.CredentialType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "temp_credential")
public class TempCredential {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "idp")
    private String idp;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = false;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "security_key", nullable = false, unique = true)
    private String securityKey;

    @Column(name = "operation", length = 50)
    private String operation;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "last_failed_attempt")
    private Date lastFailedAttempt;

    @CreatedDate
    @Column(name = "created_date")
    private Date createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 20)
    private CredentialType credentialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_adaptor", length = 50)
    private AuthAdaptor authAdaptor;

    @Column(name = "auth_type")
    private String authType;

}