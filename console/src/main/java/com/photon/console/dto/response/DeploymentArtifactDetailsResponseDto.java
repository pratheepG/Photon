package com.photon.console.dto.response;

import com.photon.console.entity.converter.ConfigMapToJsonConverter;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentArtifactDetailsResponseDto {
    private UUID id;
    private String serviceName;
    private String jarFileName;
    private String dockerfileContent;
    private Map<String, String> environment  = new HashMap<>();
    private String appId;
    private String appSecret;
    private boolean deployed;
}