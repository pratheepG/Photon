package com.photon.identity.authentication.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "otp")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "auth_type_id", nullable = false)
    private String authTypeId;

    @Column(name = "principal", nullable = false)
    private String principal;

    @Column(name = "credentials", nullable = false)
    private String credentials;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

}