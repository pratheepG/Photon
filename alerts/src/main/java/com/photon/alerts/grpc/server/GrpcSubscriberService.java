package com.photon.alerts.grpc.server;

import com.photon.alerts.dto.request.SubscriberRequestDto;
import com.photon.alerts.entity.SubscriberDeviceMap;
import com.photon.alerts.enums.SubscriberStatus;
import com.photon.alerts.repository.DeviceInfoRepository;
import com.photon.alerts.repository.SubscriberDeviceMapRepository;
import com.photon.alerts.repository.SubscriberRepository;
import com.photon.alerts.service.SubscriberService;
import com.photon.grpc.subscriber.CreateSubscriberRequest;
import com.photon.grpc.subscriber.CreateSubscriberResponse;
import com.photon.grpc.subscriber.SubscriberServiceGrpc;
import com.photon.grpc.subscriber.UpdateSubscriberRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@GrpcService
public class GrpcSubscriberService extends SubscriberServiceGrpc.SubscriberServiceImplBase {

    private final SubscriberService subscriberService;
    private final SubscriberRepository subscriberRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final SubscriberDeviceMapRepository subscriberDeviceMapRepository;

    public GrpcSubscriberService(SubscriberService subscriberService, SubscriberRepository subscriberRepository, DeviceInfoRepository deviceInfoRepository, SubscriberDeviceMapRepository subscriberDeviceMapRepository) {
        this.subscriberService = subscriberService;
        this.subscriberRepository = subscriberRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.subscriberDeviceMapRepository = subscriberDeviceMapRepository;
    }

    @Override
    public void createSubscriber(CreateSubscriberRequest request, StreamObserver<CreateSubscriberResponse> responseObserver) {
        SubscriberRequestDto dto = SubscriberRequestDto.builder()
                .userId(request.getUserId()).userName(request.getUserName()).uniqueId(request.getUniqueId())
                .subscriberName(request.getSubscriberName()).subscriberUniqueId(request.getSubscriberUniqueId())
                .email(request.getEmail()).phoneNumber(request.getPhoneNumber()).countryCode(request.getCountryCode())
                .subscriberStatus(SubscriberStatus.valueOf(request.getSubscriberStatus().name()))
                .build();

        subscriberService.createSubscriber(dto)
                .map(subscriber -> CreateSubscriberResponse.newBuilder()
                        .setSubscriberId(subscriber.getId().toString()).setStatus("SUCCESS")
                        .setMessage("Subscriber created successfully").build())
                .subscribe(response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, error -> {
                    log.error("gRPC CreateSubscriber Error: {}", error.getMessage(), error);
                    responseObserver.onError(Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
                });
    }

    @Override
    public void updateSubscriber(UpdateSubscriberRequest request, StreamObserver<CreateSubscriberResponse> responseObserver) {

        if (!StringUtils.hasText(request.getSubscriberId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("subscriber_id is required").asRuntimeException());
            return;
        }

        subscriberRepository.findById(UUID.fromString(request.getSubscriberId()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Subscriber not found for id: " + request.getSubscriberId())))
                .flatMap(existingSubscriber -> {
                    if (StringUtils.hasText(request.getSubscriberName()))
                        existingSubscriber.setSubscriberName(request.getSubscriberName());
                    if (StringUtils.hasText(request.getSubscriberUniqueId()))
                        existingSubscriber.setSubscriberUniqueId(request.getSubscriberUniqueId());
                    if (StringUtils.hasText(request.getEmail()))
                        existingSubscriber.setEmail(request.getEmail());
                    if (StringUtils.hasText(request.getPhoneNumber()))
                        existingSubscriber.setPhoneNumber(request.getPhoneNumber());
                    if (StringUtils.hasText(request.getCountryCode()))
                        existingSubscriber.setCountryCode(request.getCountryCode());
                    if (StringUtils.hasText(request.getUniqueId()))
                        existingSubscriber.setUniqueId(request.getUniqueId());
                    if (StringUtils.hasText(request.getUserId()))
                        existingSubscriber.setUserId(request.getUserId());
                    if (StringUtils.hasText(request.getUserName()))
                        existingSubscriber.setUserName(request.getUserName());

                    if (request.getSubscriberStatusValue() != 0) {
                        existingSubscriber.setSubscriberStatus(
                                SubscriberStatus.valueOf(request.getSubscriberStatus().name()));
                    }

                    return subscriberRepository.save(existingSubscriber);
                })
                .flatMap(savedSubscriber -> {
                    if (StringUtils.hasText(request.getDeviceId())) {
                        return deviceInfoRepository.findByDeviceId(request.getDeviceId())
                                .switchIfEmpty(Mono.error(new IllegalStateException("Device not found for deviceId: " + request.getDeviceId())))
                                .flatMap(deviceInfo -> {
                                    SubscriberDeviceMap map = SubscriberDeviceMap.builder()
                                            .subscriberId(savedSubscriber.getId()).deviceInfoId(deviceInfo.getId())
                                            .userId(savedSubscriber.getUserId()).isActive(true).build();
                                    return subscriberDeviceMapRepository.save(map).thenReturn(savedSubscriber);
                                });
                    }
                    return Mono.just(savedSubscriber);
                })
                .map(saved -> CreateSubscriberResponse.newBuilder()
                        .setSubscriberId(saved.getId().toString()).setStatus("SUCCESS")
                        .setMessage("Subscriber updated successfully").build())
                .subscribe(response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, error -> {
                    log.error("gRPC UpdateSubscriber Error: {}", error.getMessage(), error);
                    responseObserver.onError(Status.INTERNAL.withDescription(error.getMessage()).asRuntimeException());
                });
    }
}