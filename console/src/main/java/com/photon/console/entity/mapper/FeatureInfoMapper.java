package com.photon.console.entity.mapper;

import com.photon.console.entity.ActionInfo;
import com.photon.console.entity.FeatureInfo;
import com.photon.endpoint.dto.FeatureInfoDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class FeatureInfoMapper {

    public static FeatureInfoDto toDto(FeatureInfo featureInfo) {
        if (featureInfo == null) {
            return null;
        }

        return FeatureInfoDto.builder()
                .id(featureInfo.getId())
                .featureId(featureInfo.getFeatureId())
                .path(featureInfo.getPath())
                .name(featureInfo.getName())
                .moduleName(featureInfo.getModuleName())
                .description(featureInfo.getDescription())
                .actions(featureInfo.getActions().stream()
                        .map(ActionInfoMapper::toDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static FeatureInfo toEntity(FeatureInfoDto featureInfoDto) {
        if (featureInfoDto == null) {
            return null;
        }

        Set<ActionInfo> actions = featureInfoDto.getActions().stream()
                .map(ActionInfoMapper::toEntity)
                .collect(Collectors.toSet());

        FeatureInfo feature = FeatureInfo.builder()
                .id(featureInfoDto.getId())
                .featureId(featureInfoDto.getFeatureId())
                .path(featureInfoDto.getPath())
                .name(featureInfoDto.getName())
                .moduleName(featureInfoDto.getModuleName())
                .description(featureInfoDto.getDescription())
                .build();

        actions.forEach(actionInfo -> actionInfo.setFeature(feature));
        feature.setActions(actions);

        return feature;
    }
}