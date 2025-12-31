package com.photon.identity.commons.grpc.client;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.grpc.subscriber.*;
import com.photon.identity.authentication.dto.request.SubscriberRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriberSyncService {

    @GrpcClient("alert")
    private SubscriberServiceGrpc.SubscriberServiceBlockingStub subscriberStub;

    //private final SubscriberServiceGrpc.SubscriberServiceBlockingStub subscriberStub;

    public CreateSubscriberResponse createSubscriber(SubscriberRequestDto requestDto) throws ApplicationException {
        try {
            CreateSubscriberRequest grpcRequest = CreateSubscriberRequest.newBuilder()
                    .setUserName(Objects.requireNonNullElse(requestDto.getUserName(), ""))
                    .setUserId(Objects.requireNonNullElse(requestDto.getUserId(), ""))
                    .setUniqueId(Objects.requireNonNullElse(requestDto.getUniqueId(), ""))
                    .setSubscriberName(Objects.requireNonNullElse(requestDto.getSubscriberName(), ""))
                    .setSubscriberUniqueId(Objects.requireNonNullElse(requestDto.getSubscriberUniqueId(), ""))
                    .setEmail(Objects.requireNonNullElse(requestDto.getEmail(), ""))
                    .setPhoneNumber(Objects.requireNonNullElse(requestDto.getPhoneNumber(), ""))
                    .setCountryCode(Objects.requireNonNullElse(requestDto.getCountryCode(), ""))
                    .setSubscriberStatus(SubscriberStatus.valueOf(requestDto.getSubscriberStatus().name()))
                    .build();

            CreateSubscriberResponse response = subscriberStub.createSubscriber(grpcRequest);
            log.info("Subscriber created: {}", response.getSubscriberId());
            return response;
        } catch (Exception ex){
            log.info("Error while making createSubscriber API call: {}", ex.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CreateSubscriberResponse updateSubscriber(SubscriberRequestDto requestDto, String subscriberId) throws ApplicationException {
        try {
            UpdateSubscriberRequest grpcRequest = UpdateSubscriberRequest.newBuilder()
                    .setSubscriberId(Objects.requireNonNullElse(subscriberId, ""))
                    .setUserName(Objects.requireNonNullElse(requestDto.getUserName(), ""))
                    .setUserId(Objects.requireNonNullElse(requestDto.getUserId(), ""))
                    .setUniqueId(Objects.requireNonNullElse(requestDto.getUniqueId(), ""))
                    .setSubscriberName(Objects.requireNonNullElse(requestDto.getSubscriberName(), ""))
                    .setSubscriberUniqueId(Objects.requireNonNullElse(requestDto.getSubscriberUniqueId(), ""))
                    .setEmail(Objects.requireNonNullElse(requestDto.getEmail(), ""))
                    .setPhoneNumber(Objects.requireNonNullElse(requestDto.getPhoneNumber(), ""))
                    .setCountryCode(Objects.requireNonNullElse(requestDto.getCountryCode(), ""))
                    .setSubscriberStatus(SubscriberStatus.valueOf(requestDto.getSubscriberStatus().name()))
                    .build();

            CreateSubscriberResponse response = subscriberStub.updateSubscriber(grpcRequest);
            log.info("Subscriber updated: {}", response.getSubscriberId());
            return response;
        } catch (Exception ex){
            log.info("Error while making updated Subscriber API call: {}", ex.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}