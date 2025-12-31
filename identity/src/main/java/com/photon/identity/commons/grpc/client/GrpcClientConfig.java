package com.photon.identity.commons.grpc.client;

import com.photon.grpc.device.DeviceInfoServiceGrpc;
import com.photon.grpc.subscriber.SubscriberServiceGrpc;
import com.photon.properties.ApplicationConfigProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Slf4j
//@Configuration
//public class GrpcClientConfig {
//
//    @Bean
//    public ManagedChannel subscriberChannel(String host, int port) {
//        log.error(" Alert GRPC Host : {}, Alert GRPC Port : {}", host, port);
//        return ManagedChannelBuilder.forAddress(host, port)
//                .usePlaintext()
//                .build();
//    }
//
//    @Bean
//    public SubscriberServiceGrpc.SubscriberServiceBlockingStub subscriberBlockingStub(ApplicationConfigProperties applicationConfigProperties) {
//        return SubscriberServiceGrpc.newBlockingStub(subscriberChannel(applicationConfigProperties.getAlertGrpcHost(), applicationConfigProperties.getAlertGrpcPort()));
//    }
//
//    @Bean
//    public DeviceInfoServiceGrpc.DeviceInfoServiceBlockingStub deviceInfoBlockingStub(ApplicationConfigProperties applicationConfigProperties) {
//        return DeviceInfoServiceGrpc.newBlockingStub(subscriberChannel(applicationConfigProperties.getAlertGrpcHost(), applicationConfigProperties.getAlertGrpcPort()));
//    }
//
//}