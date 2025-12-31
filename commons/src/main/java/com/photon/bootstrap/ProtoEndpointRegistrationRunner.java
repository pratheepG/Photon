package com.photon.bootstrap;

import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.dto.mapper.DtoProtoMapper;
import com.photon.endpoint.service.EndpointScannerService;
import com.photon.grpc.endpoint.EndpointRegistryGrpc;
import com.photon.grpc.endpoint.RegisterResponse;
import com.photon.properties.ApplicationConfigProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@Order(1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ProtoEndpointRegistrationRunner implements ApplicationRunner {

    private final ManagedChannel channel;
    private final EndpointScannerService endpointScannerService;
    private final EndpointRegistryGrpc.EndpointRegistryBlockingStub blockingStub;

    public ProtoEndpointRegistrationRunner(EndpointScannerService endpointScannerService, ApplicationConfigProperties applicationConfigProperties) {
        this.endpointScannerService = endpointScannerService;
        this.channel = ManagedChannelBuilder.forAddress(applicationConfigProperties.getConsoleGrpcHost(), applicationConfigProperties.getConsoleGrpcPort()).usePlaintext().build();
        this.blockingStub = EndpointRegistryGrpc.newBlockingStub(channel);
    }

    @Async
    @Override
    public void run(ApplicationArguments args) {
        try {
            EndpointDetailsDto endpointDetails = this.endpointScannerService.scanEndpoints().getResponseData();
            com.photon.grpc.endpoint.EndpointDetails proto = DtoProtoMapper.toProto(endpointDetails);
            RegisterResponse resp = blockingStub.registerEndpoints(proto);
            log.warn("Register endpoints response : {} ", resp);
        } catch (Exception e) {
            log.error("Error while registering endpoints", e);
        } finally {
            channel.shutdownNow();
        }
    }
}