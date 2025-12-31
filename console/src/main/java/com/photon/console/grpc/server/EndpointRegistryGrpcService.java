package com.photon.console.grpc.server;

import com.photon.endpoint.dto.EndpointDetailsDto;
import com.photon.endpoint.dto.mapper.ProtoToDtoMapper;
import com.photon.grpc.endpoint.EndpointDetails;
import com.photon.grpc.endpoint.RegisterResponse;
import com.photon.grpc.endpoint.EndpointRegistryGrpc;
import com.photon.console.service.ApiManagerService;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@GrpcService
public class EndpointRegistryGrpcService extends EndpointRegistryGrpc.EndpointRegistryImplBase {

    private final ApiManagerService apiManagerService;

    @Autowired
    public EndpointRegistryGrpcService(ApiManagerService apiManagerService) {
        this.apiManagerService = apiManagerService;
    }

    @Override
    public void registerEndpoints(EndpointDetails protoRequest, StreamObserver<RegisterResponse> responseObserver) {
        try {
            log.info("Received gRPC registerEndpoints request for id={}, name={}", protoRequest.getId(), protoRequest.getName());
            EndpointDetailsDto dto = ProtoToDtoMapper.fromProto(protoRequest);

            this.apiManagerService.synchronizeAllFeatureAction(dto.getId(), dto);
            RegisterResponse resp = RegisterResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Registration accepted")
                    .build();

            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Error handling registerEndpoints", ex);
            RegisterResponse resp = RegisterResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error: " + ex.getMessage())
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }
    }
}
