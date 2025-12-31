package com.photon.apiconfig.entity.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.photon.apiconfig.dto.ConfigPropertiesDto;
import com.photon.apiconfig.entity.ConfigProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.HashMap;

@Component // Mark this as a Spring component to allow dependency injection
public class ConfigPropertiesMapper {

    private final ObjectMapper objectMapper; // Inject ObjectMapper

    // Constructor injection for ObjectMapper
    public ConfigPropertiesMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Helper method to convert Map<String, String> to JsonNode.
     * Used when converting DTO's config to Entity's config.
     * @param configMap The map to convert.
     * @return A JsonNode representing the map, or null if input map is null.
     */
    public JsonNode toConfigJsonNode(Map<String, String> configMap) {
        if (configMap == null) {
            return null;
        }
        return objectMapper.valueToTree(configMap);
    }

    /**
     * Helper method to convert JsonNode to Map<String, String>.
     * Used when converting Entity's config to DTO's config.
     * @param configJsonNode The JsonNode to convert.
     * @return A Map<String, String> representing the JsonNode, or an empty map if input is null or not an object.
     */
    public Map<String, String> toConfigMap(JsonNode configJsonNode) {
        if (configJsonNode == null || configJsonNode.isNull()) {
            return new HashMap<>(); // Return empty map for null or empty JsonNode
        }
        // Ensure it's an object node before converting to map
        if (configJsonNode.isObject()) {
            return objectMapper.convertValue(configJsonNode, new TypeReference<Map<String, String>>() {});
        }
        // If JsonNode is not an object (e.g., array, primitive), return empty map or handle as error
        return new HashMap<>();
    }

    /**
     * Converts a ConfigPropertiesDto to a ConfigProperties entity.
     * @param dto The DTO to convert.
     * @return The converted entity.
     */
    public ConfigProperties toEntity(ConfigPropertiesDto dto) {
        if (dto == null) {
            return null;
        }
        ConfigProperties entity = new ConfigProperties();
        entity.setId(dto.getId());
        entity.setProfile(dto.getProfile());

        // Use helper method for conversion
        entity.setConfig(toConfigJsonNode(dto.getConfig()));
        return entity;
    }

    /**
     * Converts a ConfigProperties entity to a ConfigPropertiesDto.
     * @param entity The entity to convert.
     * @return The converted DTO.
     */
    public ConfigPropertiesDto toDto(ConfigProperties entity) {
        if (entity == null) {
            return null;
        }
        ConfigPropertiesDto dto = new ConfigPropertiesDto();
        dto.setId(entity.getId());
        dto.setProfile(entity.getProfile());

        // Use helper method for conversion
        dto.setConfig(toConfigMap(entity.getConfig()));
        return dto;
    }

    /**
     * Partially updates an existing ConfigProperties entity from ConfigPropertiesDto.
     * Only non-null and non-empty fields from the DTO will update the entity.
     * @param dto The DTO containing update data.
     * @param entity The existing entity to update.
     */
    public void partialUpdate(ConfigPropertiesDto dto, ConfigProperties entity) {
        if (dto == null || entity == null) {
            return;
        }
        // ID and Profile updates remain the same
        if (!ObjectUtils.isEmpty(dto.getId())) {
            entity.setId(dto.getId());
        }
        if (!ObjectUtils.isEmpty(dto.getProfile())) {
            entity.setProfile(dto.getProfile());
        }

        // Convert DTO's Map to Entity's JsonNode using the helper
        // This will replace the entire 'config' JsonNode if a new map is provided in the DTO
        if (dto.getConfig() != null) {
            entity.setConfig(toConfigJsonNode(dto.getConfig()));
        }
        // If dto.getConfig() is explicitly null and you want to null out the entity's config, uncomment below:
        // else if (dto.getConfig() == null && entity.getConfig() != null) {
        //     entity.setConfig(null);
        // }
    }
}