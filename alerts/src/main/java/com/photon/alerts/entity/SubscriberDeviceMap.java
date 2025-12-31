package com.photon.alerts.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("subscriber_device_map")
public class SubscriberDeviceMap {

    @Column("subscriber_id")
    private UUID subscriberId;

    @Column("device_info_id")
    private UUID deviceInfoId;

    @Column("user_id")
    private String userId;

    @Column("is_active")
    private Boolean isActive;

}