package com.photon.alerts.entity;

import com.photon.alerts.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("alert_template")
public class AlertTemplate {
    @Id
    private Long id;

    @Column("template_id")
    private String templateId;

    @Column("alert_id")
    private Long alertId;

    private Channel channel;

    @Column("subject_template")
    private String subjectTemplate;

    @Column("message_template")
    private String messageTemplate;

    @Column("sms_deeplink_url")
    private String smsDeeplinkUrl;

    @Column("sms_media_url")
    private String smsMediaUrl;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;
}