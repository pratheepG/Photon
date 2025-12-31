package com.photon.console.entity;

import com.photon.console.entity.converter.SetOfMapToJsonConverter;
import jakarta.persistence.*;

import lombok.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "model")
public class Model {

    @Id
    private String id;

    @Column(name = "model_id", nullable = false)
    private String modelId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Convert(converter = SetOfMapToJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private Set<Map<String, String>> fields = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_details_id", nullable = false)
    private EndpointDetails endpointDetails;
}