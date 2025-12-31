package com.photon.alerts.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("device_info")
public class DeviceInfo {

    @Id
    private UUID id;

    @Column("device_type")
    private String deviceType;

    @Column("device_id")
    private String deviceId;

    @Column("is_active")
    private Boolean isActive;

    @Column("reg_id")
    private String regId;

    @Column("platform")
    private String platform;

    @Column("os_version")
    private String osVersion;

    @Column("app_version")
    private String appVersion;

    @Column("device_model")
    private String deviceModel;

    @Column("device_brand")
    private String deviceBrand;

    @Column("browser_version")
    private String browserVersion;

    @Column("screen_resolution")
    private String screenResolution;

    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column("last_active")
    private LocalDateTime lastActive;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;
}