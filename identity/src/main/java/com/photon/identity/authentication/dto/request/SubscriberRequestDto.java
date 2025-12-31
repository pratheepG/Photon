package com.photon.identity.authentication.dto.request;

import com.photon.identity.commons.enums.SubscriberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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