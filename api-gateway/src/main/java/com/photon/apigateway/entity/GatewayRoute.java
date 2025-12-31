package com.photon.apigateway.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Setter
@Getter
@Table("gateway_routes")
public class GatewayRoute {

    @Id
    private Long id;

    @Column("route_id")
    private String routeId;

    @Column("route_definition")
    private String routeDefinition;

    @Version
    private Long version;

}