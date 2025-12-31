package com.photon.alerts.dto.request;

import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSubscriptionRequestDto {
    private Channel channel;
    private Boolean isSubscribed;
    private String regId;
    private String deviceId;
}