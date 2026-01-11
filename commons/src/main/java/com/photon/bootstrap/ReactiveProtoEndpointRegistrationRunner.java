package com.photon.bootstrap;

import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.service.ReactiveEndpointScannerService;
import com.photon.grpc.endpoint.EndpointRegistryGrpc;
import com.photon.grpc.endpoint.RegisterResponse;
import com.photon.endpoint.dto.mapper.DtoProtoMapper;
import com.photon.properties.ApplicationConfigProperties;
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
    private final EndpointRegistryGrpc.EndpointRegistryBlockingStub blockingStub;

    public ReactiveProtoEndpointRegistrationRunner(
            @GrpcClient("console") EndpointRegistryGrpc.EndpointRegistryBlockingStub blockingStub,
            ReactiveEndpointScannerService endpointScannerService, ApplicationConfigProperties applicationConfigProperties) {
        this.endpointScannerService = endpointScannerService;
        this.blockingStub = blockingStub;
        this.applicationConfigProperties = applicationConfigProperties;
    }

    @Override
    public void run(ApplicationArguments args) {

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
                })
                .subscribe();
    }
}