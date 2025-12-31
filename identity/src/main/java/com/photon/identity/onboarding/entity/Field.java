package com.photon.identity.onboarding.entity;

import com.photon.identity.onboarding.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "field")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private FieldType type;

    @Column(name = "is_collection")
    private boolean isCollection;

    @Column(name = "field", columnDefinition = "TEXT")
    private String field;
}