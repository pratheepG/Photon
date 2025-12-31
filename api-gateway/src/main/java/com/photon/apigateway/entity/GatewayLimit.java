package com.photon.apigateway.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@Table("gateway_limits")
public class GatewayLimit {

  @Id
  private Long id;

  @Column("key_name")
  private String keyName;

  @Column("value_bigint")
  private Long value;

  @Column("description")
  private String description;

}