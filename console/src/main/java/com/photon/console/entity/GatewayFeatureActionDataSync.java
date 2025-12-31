package com.photon.console.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gateway_feature_action_data_sync")
public class GatewayFeatureActionDataSync {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "gateway_route_id", nullable = false,  unique = true)
    private String gatewayRouteId;

    @Column(name = "role_feature_action", nullable = false,  unique = true)
    private String roleFeatureAction;

}