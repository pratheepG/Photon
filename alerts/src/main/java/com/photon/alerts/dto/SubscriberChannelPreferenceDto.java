package com.photon.alerts.dto;

import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberChannelPreferenceDto {
    private Channel channel;
    private Boolean isSubscribed;
}