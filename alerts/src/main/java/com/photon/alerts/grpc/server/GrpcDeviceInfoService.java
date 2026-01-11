package com.photon.alerts.grpc.server;

import com.photon.alerts.entity.DeviceInfo;
import com.photon.alerts.service.DeviceInfoService;
import com.photon.grpc.device.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcDeviceInfoService extends DeviceInfoServiceGrpc.DeviceInfoServiceImplBase {

    private final DeviceInfoService deviceInfoService;

    @Override
    public void createDeviceInfo(CreateDeviceInfoRequest request, StreamObserver<CreateDeviceInfoResponse> responseObserver) {
        DeviceInfo entity = DeviceInfo.builder()
                .deviceId(request.getDeviceId())
                .deviceType(request.getDeviceType())
                .platform(request.getPlatform())
                .regId(request.getRegId())
                .osVersion(request.getOsVersion())
                .appVersion(request.getAppVersion())
                .deviceModel(request.getDeviceModel())
                .deviceBrand(request.getDeviceBrand())
                .browserVersion(request.getBrowserVersion())
                .screenResolution(request.getScreenResolution())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        deviceInfoService.save(entity)
                .doOnNext(saved -> {
                    CreateDeviceInfoResponse response = CreateDeviceInfoResponse.newBuilder()
                            .setId(saved.getResponseData().getId().toString())
                            .setStatus("SUCCESS")
                            .setMessage("DeviceInfo created")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                })
                .doOnError(error -> {
                    log.error("DeviceInfo creation failed", error);
                    responseObserver.onError(Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
                })
                .subscribe();
    }

    @Override
    public void linkDeviceToSubscriber(LinkDeviceRequest request, StreamObserver<LinkDeviceResponse> responseObserver) {
        UUID subscriberId = UUID.fromString(request.getSubscriberId());
        String userId = request.getUserId();

        deviceInfoService.linkDeviceToSubscriber(subscriberId, request.getDeviceId(), userId)
                .subscribe(result -> {
                    LinkDeviceResponse response = LinkDeviceResponse.newBuilder()
                            .setStatus("SUCCESS")
                            .setMessage("Device linked successfully")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, error -> {
                    log.error("gRPC LinkDeviceToSubscriber error", error);
                    responseObserver.onError(Status.INTERNAL
                            .withDescription("Failed to link device to subscriber")
                            .withCause(error)
                            .asRuntimeException());
                });
    }
}