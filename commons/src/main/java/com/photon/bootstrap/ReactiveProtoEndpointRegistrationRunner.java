package com.photon.bootstrap;

import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.service.ReactiveEndpointScannerService;
import com.photon.grpc.endpoint.EndpointRegistryGrpc;
import com.photon.grpc.endpoint.RegisterResponse;
import com.photon.endpoint.dto.mapper.DtoProtoMapper;
import com.photon.properties.ApplicationConfigProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Order(1)
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveProtoEndpointRegistrationRunner implements ApplicationRunner {

    private final ReactiveEndpointScannerService endpointScannerService;
    private final ApplicationConfigProperties applicationConfigProperties;

    @GrpcClient("console")
    private EndpointRegistryGrpc.EndpointRegistryBlockingStub blockingStub;

    public ReactiveProtoEndpointRegistrationRunner(ReactiveEndpointScannerService endpointScannerService,
                                                   ApplicationConfigProperties applicationConfigProperties) {
        this.endpointScannerService = endpointScannerService;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    @Override
    public void run(ApplicationArguments args) {

        String host = applicationConfigProperties.getConsoleGrpcHost();
        int port = applicationConfigProperties.getConsoleGrpcPort();

        if (host == null || host.isBlank() || port <= 0) {
            log.info("Console gRPC host/port not configured. Skipping gRPC endpoint registration.");
            return;
        }

//        ManagedChannel channel = ManagedChannelBuilder
//                .forAddress(host, port)
//                .usePlaintext()
//                .build();

//        EndpointRegistryGrpc.EndpointRegistryBlockingStub blockingStub =
//                EndpointRegistryGrpc.newBlockingStub(channel);

        endpointScannerService.scanEndpoints()
                .map(response -> {
                    EndpointDetailsDto dto = EndpointDetailsDto.builder()
                            .features(response.getResponseData().getFeatures())
                            .id(applicationConfigProperties.getApplicationName())
                            .name(applicationConfigProperties.getApplicationName())
                            .clientId(applicationConfigProperties.getXApiKey())
                            .clientSecret(applicationConfigProperties.getXApiSecret())
                            .models(response.getResponseData().getModels())
                            .build();

                    return DtoProtoMapper.toProto(dto);
                })
                .flatMap(proto ->
                        Mono.fromCallable(() -> {
                                    RegisterResponse resp = blockingStub.registerEndpoints(proto);
                                    log.warn("Register endpoints (gRPC) response: {}", resp);
                                    return resp;
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .doOnError(e -> log.error("Error while registering endpoints via gRPC", e))
                .doFinally(signal -> {
                    log.info("Shutting down gRPC channel, signal={}", signal);
                    //channel.shutdownNow();
                })
                .subscribe();
    }
}