package com.photon.console.entity.mapper;

import com.photon.console.entity.EndpointDetails;
import com.photon.console.entity.FeatureInfo;
import com.photon.console.entity.Model;
import com.photon.endpoint.dto.EndpointDetailsDto;

import java.util.Set;
import java.util.stream.Collectors;

public class EndpointDetailsMapper {

    public static EndpointDetailsDto toDto(EndpointDetails endpointDetails) {
        if(endpointDetails == null) return null;

        return EndpointDetailsDto.builder()
                .id(endpointDetails.getId())
                .name(endpointDetails.getName())
                .clientId(endpointDetails.getClientId())
                .clientSecret(endpointDetails.getClientSecret())
                .features(endpointDetails.getFeatures().stream().map(FeatureInfoMapper::toDto).collect(Collectors.toSet()))
                .models(endpointDetails.getModels().stream().map(ModelMapper::toDto).collect(Collectors.toSet()))
                .build();
    }

    public static EndpointDetails toEntity(EndpointDetailsDto endpointDetailsDto) {
        if (endpointDetailsDto == null) return null;

        Set<FeatureInfo> features = endpointDetailsDto.getFeatures().stream()
                .map(FeatureInfoMapper::toEntity)
                .collect(Collectors.toSet());

//        Set<Model> models = endpointDetailsDto.getModels().stream()
//                .map(ModelMapper::toEntity)
//                .collect(Collectors.toSet());

        EndpointDetails endpointDetails = EndpointDetails.builder()
                .id(endpointDetailsDto.getId())
                .name(endpointDetailsDto.getName())
                .clientSecret(endpointDetailsDto.getClientSecret())
                .clientId(endpointDetailsDto.getClientId())
                .build();

        features.forEach(featureInfo -> featureInfo.setEndpointDetails(endpointDetails));
        endpointDetails.setFeatures(features);
        return endpointDetails;
    }

}