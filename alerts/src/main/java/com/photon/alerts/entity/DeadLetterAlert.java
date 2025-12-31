package com.photon.alerts.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@Table("dead_letter_alert")
public class DeadLetterAlert {
    @Id
    private Long id;

    private String alertId;
    private String alertType;
    private String channel;
    private String reason;

    @Column("alert_json")
    private String originalAlertJson;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;

    @Column("manually_processed")
    private boolean manuallyProcessed;
}