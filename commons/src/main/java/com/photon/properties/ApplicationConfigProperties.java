package com.photon.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serializable;

@Getter
@RefreshScope
public class ApplicationConfigProperties implements Serializable {

    @Value("${db.port:#{null}}")
    private String dbPort;

    @Value("${db.schema-name:#{null}}")
    private String dbName;

    @Value("${db.host:#{null}}")
    private String dbHostName;

    @Value("${spring.datasource.username:#{null}}")
    private String dbUserName;

    @Value("${spring.datasource.password:#{null}}")
    private String dbPassword;

    @Value("${spring.application.name:#{null}}")
    private String applicationName;

    @Value("${spring.security.user.name:#{null}}")
    private String xApiKey;

    @Value("${spring.security.user.password:#{null}}")
    private String xApiSecret;

    @Value("${photon.api.key:#{null}}")
    private String compositeXApiKey;

    @Value("${photon.api.secret:#{null}}")
    private String compositeXApiSecret;

    @Value("${spring.redis.host:#{null}}")
    private String redisHost;

    @Value("${spring.redis.port:#{null}}")
    private Integer redisPort;

    @Value("${spring.event.broker:#{null}}")
    private String eventBroker;

    @Value("${photon.grpc.alert.host:#{null}}")
    private String alertGrpcHost;

    @Value("${photon.grpc.alert.port:0000}")
    private int alertGrpcPort;

    @Value("${photon.grpc.console.host:#{null}}")
    private String consoleGrpcHost;

    @Value("${photon.grpc.console.port:0000}")
    private int consoleGrpcPort;

    @Value("${photon.grpc.identity.host:#{null}}")
    private String identityGrpcHost;

    @Value("${photon.grpc.identity.port:0000}")
    private int identityGrpcPort;

    @Value("${photon.grpc.storage.host:#{null}}")
    private String storageGrpcHost;

    @Value("${photon.grpc.storage.port:0000}")
    private int storageGrpcPort;

}