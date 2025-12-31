package com.photon.identity.idp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mfa_condition_group")
public class MFAConditionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "operator", nullable = false)
    private String operator;

    @JsonIgnoreProperties({"group"})
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<MFAConditionGroupItem> items = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "condition_set_id", nullable = false)
    private MFAConditionSet conditionSet;

}