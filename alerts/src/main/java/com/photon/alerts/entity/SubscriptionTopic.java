package com.photon.alerts.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Table("subscription_topic")
public class SubscriptionTopic {
    @Id
    private UUID id;

    @Column("topic")
    private String topic;

    @Column("created_at")
    private LocalDateTime createdAt;
}