package com.photon.identity.idp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
@Entity
@Table(name = "sca_configuration")
public class SCAConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String description;
    private boolean isActive = true;
    private boolean isPreLogin = false;

    @ManyToOne
    @JoinColumn(name = "first_factor_id")
    private AuthType firstFactor;

    @ManyToMany
    @JoinTable(name = "sca_configuration_second_factors",
            joinColumns = @JoinColumn(name = "sca_configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "auth_types_id"))
    private Set<AuthType> secondFactors = new LinkedHashSet<>();

}