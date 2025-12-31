package com.photon.identity.idp.entity;

import com.photon.identity.commons.enums.IdentityProviderType;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "identity_provider")
public class IdentityProvider {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @NotBlank(message = "Identity Provider id can't be Blank")
    @NotEmpty(message = "Identity Provider id can't be Empty")
    @NotNull(message = "Identity Provider id can't be Null")
    private String id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "idp_default_role")
    private Long idpDefaultRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_provider_type", nullable = false)
    private IdentityProviderType identityProviderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_algorithm")
    private SignatureAlgorithm signatureAlgorithm;

    @ManyToOne
    @JoinColumn(name = "cert_id", nullable = false)
    private Certificate certificate;

    @Column(name = "session_timeout_minutes", nullable = false)
    private Integer sessionTimeoutMinutes;

    @Column(name = "session_idle_timeout_minutes")
    private Integer sessionIdleTimeoutMinutes;

    @Column(name = "active_sessions", nullable = false)
    private Integer activeSessions;

    @Column(name = "refresh_token_session_timeout_minutes")
    private Integer refreshTokenSessionTimeoutMinutes;

    @OneToMany(mappedBy = "identityProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IdentityProviderAuthType> identityProviderAuthTypes = new LinkedHashSet<>();

    @Column(name = "is_mfa_enabled")
    private Boolean isMfaEnabled = false;

    @Column(name = "is_mfa_required_for_every_login")
    private Boolean isMfaRequiredForEveryLogin = false;

    @Column(name = "is_mfa_condition_check_enabled")
    private Boolean isMfaConditionCheckEnabled = false;

    @Column(name = "include_x509_in_jwt", nullable = false)
    private Boolean includeX509InJwt = false;

    @Column(name = "last_validated_mfa_expires_in_minutes")
    private Long lastValidateMfaExpiresInMinutes = 0L;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mfa_condition_id")
    private MFAConditionSet mfaCondition;
}