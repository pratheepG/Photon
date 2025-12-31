package com.photon.identity.idp.dto;

import com.photon.identity.idp.entity.SCAConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link SCAConfiguration}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SCAConfigurationDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private boolean isActive = true;
    private boolean isPreLogin = false;
    private String firstFactor;
    private Set<String> secondFactors = new LinkedHashSet<>();
}