package com.photon.alerts.entity;

import com.photon.alerts.enums.SubscriberStatus;
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
@Table("subscriber")
public class Subscriber {

    @Id
    private UUID id;

    @Column("user_name")
    private String userName;

    @Column("user_id")
    private String userId;

    @Column("unique_id")
    private String uniqueId;

    @Column("subscriber_name")
    private String subscriberName;

    @Column("subscriber_unique_id")
    private String subscriberUniqueId;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;

    @Column("country_code")
    private String countryCode;

    @Column("subscriber_status")
    private SubscriberStatus subscriberStatus;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;

}