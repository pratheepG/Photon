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
@Table("subscriber_topic")
public class SubscriberTopicMap {
    @Id
    private UUID id;

    @Column("subscriber_id")
    private UUID subscriberId;

    @Column("topic_id")
    private UUID topicId;

    @Column("topic")
    private String topic;

    @Column("subscribed_at")
    private LocalDateTime subscribedAt;
}