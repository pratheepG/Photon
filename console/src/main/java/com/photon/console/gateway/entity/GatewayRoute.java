package com.photon.console.gateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gateway_routes",
        uniqueConstraints = @UniqueConstraint(name = "uk_gateway_routes_route_id", columnNames = {"route_id"}),
        indexes = @Index(name = "idx_gateway_routes_route_id", columnList = "route_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "route_definition", columnDefinition = "TEXT", nullable = false)
    private String routeDefinition;

    @Version
    private Long version;

    public GatewayRoute(String routeId, String routeDefinition) {
        this.routeId = routeId;
        this.routeDefinition = routeDefinition;
    }
}