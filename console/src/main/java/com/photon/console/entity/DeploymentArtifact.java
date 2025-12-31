package com.photon.console.entity;

import com.photon.console.entity.converter.ConfigMapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity @Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deployment_artifact")
public class DeploymentArtifact {

    @Id
    @GeneratedValue
    private UUID id;

    private String serviceName;

    private String jarFileName;

    @Lob
    private byte[] jarContent;

    @Lob
    private String dockerfileContent;

    @Convert(converter = ConfigMapToJsonConverter.class)
    @Column(name = "environment", columnDefinition = "TEXT")
    private Map<String, String> environment  = new HashMap<>();

    private String appId;

    private String appSecret;

    private boolean deployed;
}