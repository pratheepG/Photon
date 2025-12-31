package com.photon.alerts.dto.request;

import com.photon.alerts.enums.SubscriberStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberRequestDto {
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
}