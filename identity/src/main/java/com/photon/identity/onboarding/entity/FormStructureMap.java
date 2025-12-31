package com.photon.identity.onboarding.entity;

import com.photon.identity.onboarding.enums.StructureItemType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "form_structure_map")
public class FormStructureMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Form form;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private StructureItemType type;

    private String dtoFieldName;

    @Column(name = "is_required")
    private boolean required;
}