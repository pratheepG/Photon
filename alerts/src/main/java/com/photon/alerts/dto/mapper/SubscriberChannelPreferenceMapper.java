package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.SubscriberChannelPreferenceDto;
import com.photon.alerts.entity.SubscriberChannelPreference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SubscriberChannelPreferenceMapper {

    public static List<SubscriberChannelPreference> toEntities(UUID subscriberId, List<SubscriberChannelPreferenceDto> dtos) {
        return dtos.stream()
                .map(dto -> SubscriberChannelPreference.builder()
                        .id(UUID.randomUUID())
                        .subscriberId(subscriberId)
                        .channel(dto.getChannel())
                        .isSubscribed(dto.getIsSubscribed())
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<SubscriberChannelPreferenceDto> toDtos(List<SubscriberChannelPreference> entities) {
        return entities.stream()
                .map(e -> SubscriberChannelPreferenceDto.builder()
                        .channel(e.getChannel())
                        .isSubscribed(e.getIsSubscribed())
                        .build())
                .collect(Collectors.toList());
    }
}
