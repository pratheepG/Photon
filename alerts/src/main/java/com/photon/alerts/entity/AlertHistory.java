package com.photon.alerts.entity;

import com.photon.alerts.enums.AlertStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@Table("alert_history")
public class AlertHistory {
    @Id
    private Long id;

    private String alertId;
    private AlertStatus status;
    private String channel;
    private String payload;
    private String failureReason;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;
}