package com.photon.alerts.dto.mapper;

import com.photon.alerts.dto.request.SubscriberRequestDto;
import com.photon.alerts.dto.response.SubscriberResponseDto;
import com.photon.alerts.entity.Subscriber;

import java.time.LocalDateTime;

public class SubscriberMapper {

    public static Subscriber toEntity(SubscriberRequestDto dto) {
        return Subscriber.builder()
                .userName(dto.getUserName())
                .userId(dto.getUserId())
                .uniqueId(dto.getUniqueId())
                .subscriberName(dto.getSubscriberName())
                .subscriberUniqueId(dto.getSubscriberUniqueId())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .countryCode(dto.getCountryCode())
                .subscriberStatus(dto.getSubscriberStatus())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    public static SubscriberResponseDto toDto(Subscriber s) {
        return SubscriberResponseDto.builder()
                .id(s.getId())
                .userName(s.getUserName())
                .userId(s.getUserId())
                .uniqueId(s.getUniqueId())
                .subscriberName(s.getSubscriberName())
                .subscriberUniqueId(s.getSubscriberUniqueId())
                .email(s.getEmail())
                .phoneNumber(s.getPhoneNumber())
                .countryCode(s.getCountryCode())
                .subscriberStatus(s.getSubscriberStatus())
                .createdAt(s.getCreatedAt())
                .modifiedAt(s.getModifiedAt())
                .build();
    }
}
