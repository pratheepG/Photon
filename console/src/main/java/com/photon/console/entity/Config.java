package com.photon.console.entity;

import com.photon.console.entity.converter.ConfigMapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Builder
@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "config")
public class Config {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Convert(converter = ConfigMapToJsonConverter.class)
    @Column(name = "config", columnDefinition = "json")
    private Map<String, String> config;
}