package com.photon.apiconfig.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO for {@link com.photon.apiconfig.entity.ConfigProperties}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigPropertiesDto implements Serializable {
    String id;
    String profile;
    Map<String, String> config;
}