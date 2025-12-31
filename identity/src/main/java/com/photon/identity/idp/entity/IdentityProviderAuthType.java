package com.photon.identity.idp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "identity_provider_auth_type")
public class IdentityProviderAuthType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "first_factor_id", nullable = false)
    private AuthType firstFactor;

    @ManyToMany
    @JoinTable(name = "identity_provider_second_factor",
            joinColumns = @JoinColumn(name = "identity_provider_auth_type_id"),
            inverseJoinColumns = @JoinColumn(name = "auth_type_id"))
    private Set<AuthType> secondFactors = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "identity_provider_id")
    private IdentityProvider identityProvider;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        IdentityProviderAuthType that = (IdentityProviderAuthType) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    /**
     * Validates that the firstFactor is not present in secondFactors.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidAuthTypeSelection() {
        if (firstFactor == null || secondFactors == null || secondFactors.isEmpty()) {
            return true;
        }
        return secondFactors.stream().noneMatch(authType -> authType.getId().equals(firstFactor.getId()));
    }

    /**
     * Returns validation errors if any.
     *
     * @return validation message or null if valid.
     */
    public String validate() {
        if (!isValidAuthTypeSelection()) {
            return "The firstFactor cannot be present in secondFactors.";
        }
        return null;
    }
}