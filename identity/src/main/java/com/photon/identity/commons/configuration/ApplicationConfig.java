package com.photon.identity.commons.configuration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.TimeUnit;

@EnableAsync
@Configuration
public class ApplicationConfig {

    @Bean
    public Cache<String, Object> guavaCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(20)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> {
            if (serverBuilder instanceof NettyServerBuilder) {
                ((NettyServerBuilder) serverBuilder)
                        .maxConnectionIdle(10, TimeUnit.SECONDS);
            }
        };
    }
}