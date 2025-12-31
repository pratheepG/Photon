package com.photon.identity.idp.entity;

import com.photon.identity.commons.enums.MFAConditionType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mfa_condition_group_item")
public class MFAConditionGroupItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "operator", nullable = false)
    private String operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private MFAConditionType condition;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private MFAConditionGroup group;
}