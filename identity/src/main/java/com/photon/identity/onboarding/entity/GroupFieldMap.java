package com.photon.identity.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_field_map")
public class GroupFieldMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Group group;

    @ManyToOne
    private Field field;

    private String dtoFieldName; // to make the dto request object

    @Column(name = "is_required")
    private boolean isRequired;
}