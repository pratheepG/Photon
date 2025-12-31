package com.photon.alerts.entity;

import com.photon.alerts.enums.AlertAudience;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@Table("alert")
public class Alert {
    @Id
    private Long id;

    private String alertType;
    private String alertSubType;

    private AlertAudience audience;
    private String topic;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;
}