package com.photon.alerts.dto.response;

import com.photon.alerts.enums.SubscriberStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberResponseDto {
    private UUID id;
    private String userName;
    private String userId;
    private String uniqueId;
    private String subscriberName;
    private String subscriberUniqueId;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private SubscriberStatus subscriberStatus;
    private String deviceId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}