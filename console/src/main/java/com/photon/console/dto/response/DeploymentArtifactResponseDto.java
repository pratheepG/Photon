package com.photon.console.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentArtifactResponseDto {
    private UUID id;
    private String serviceName;
    private String jarFileName;
    private boolean deployed;
}