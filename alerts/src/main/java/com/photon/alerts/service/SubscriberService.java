package com.photon.alerts.service;

import com.photon.alerts.dto.request.ChannelSubscriptionRequestDto;
import com.photon.alerts.entity.DeviceInfo;
import com.photon.alerts.entity.SubscriberChannelPreference;
import com.photon.alerts.entity.SubscriberTopicMap;
import com.photon.alerts.enums.Channel;
import com.photon.alerts.repository.DeviceInfoRepository;
import com.photon.alerts.repository.SubscriberChannelPreferenceRepository;
import com.photon.alerts.repository.SubscriberTopicMapRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.alerts.dto.request.SubscriberRequestDto;
import com.photon.alerts.dto.response.SubscriberResponseDto;
import com.photon.alerts.entity.Subscriber;
import com.photon.alerts.dto.mapper.SubscriberMapper;
import com.photon.alerts.repository.SubscriberRepository;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final SubscriberTopicMapRepository subscriberTopicMapRepository;
    private final TransactionalOperator transactionalOperator;
    private final SubscriberChannelPreferenceRepository subscriberChannelPreferenceRepository;
    private final DeviceInfoRepository deviceInfoRepository;

    public SubscriberService(SubscriberRepository subscriberRepository, SubscriberTopicMapRepository subscriberTopicMapRepository, TransactionalOperator transactionalOperator, SubscriberChannelPreferenceRepository subscriberChannelPreferenceRepository, DeviceInfoRepository deviceInfoRepository) {
        this.subscriberRepository = subscriberRepository;
        this.subscriberTopicMapRepository = subscriberTopicMapRepository;
        this.transactionalOperator = transactionalOperator;
        this.subscriberChannelPreferenceRepository = subscriberChannelPreferenceRepository;
        this.deviceInfoRepository = deviceInfoRepository;
    }

    public Flux<Subscriber> getSubscribersByTopic(String topic) {
        return this.subscriberTopicMapRepository.findSubscriberTopicMapByTopic(topic)
                .map(SubscriberTopicMap::getSubscriberId)
                .buffer(100)
                .flatMap(subscriberRepository::findSubscribersByIdIn)
                .onErrorResume(e -> {
                    log.error("❌ Failed to fetch subscribers by topic: {}", e.getMessage(), e);
                    return Flux.empty();
                });
    }

    public Mono<Subscriber> createSubscriber(SubscriberRequestDto dto) {
        Subscriber subscriber = SubscriberMapper.toEntity(dto);
        return subscriberRepository.save(subscriber);
    }

    public Mono<ApiResponseDto<SubscriberResponseDto>> create(SubscriberRequestDto request) {
        Subscriber subscriber = SubscriberMapper.toEntity(request);

        return transactionalOperator.transactional(
                subscriberRepository.save(subscriber)
                        .map(SubscriberMapper::toDto)
                        .map(SuccessEnum.CREATED::getSuccessResponseBody)
                        .onErrorResume(e -> {
                            log.error("Error in createSubscriber: {}", e.getMessage(), e);
                            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to create Subscriber"), HttpStatus.INTERNAL_SERVER_ERROR));
                        })
        );
    }

    public Mono<ApiResponseDto<SubscriberResponseDto>> getById(UUID id) {
        return subscriberRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody("Subscriber not found"), HttpStatus.NOT_FOUND)))
                .map(SubscriberMapper::toDto)
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<SubscriberResponseDto>> update(UUID id, SubscriberRequestDto request) {
        return subscriberRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody("Subscriber not found"), HttpStatus.NOT_FOUND)))
                .flatMap(existing -> {
                    existing.setUserName(request.getUserName());
                    existing.setUserId(request.getUserId());
                    existing.setUniqueId(request.getUniqueId());
                    existing.setSubscriberName(request.getSubscriberName());
                    existing.setSubscriberUniqueId(request.getSubscriberUniqueId());
                    existing.setEmail(request.getEmail());
                    existing.setPhoneNumber(request.getPhoneNumber());
                    existing.setCountryCode(request.getCountryCode());
                    existing.setSubscriberStatus(request.getSubscriberStatus());
                    existing.setModifiedAt(LocalDateTime.now());
                    return subscriberRepository.save(existing);
                })
                .map(SubscriberMapper::toDto)
                .map(SuccessEnum.UPDATED::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<?>> delete(UUID id) {
        return subscriberRepository.deleteById(id)
                .thenReturn(SuccessEnum.DELETED.getSuccessResponseBody());
    }

    public Mono<ApiResponseDto<SubscriberResponseDto>> findByUserId(String userId) {
        return subscriberRepository.findByUserId(userId)
                .map(SubscriberMapper::toDto)
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<SubscriberResponseDto>> findByUniqueId(String uniqueId) {
        return subscriberRepository.findByUniqueId(uniqueId)
                .map(SubscriberMapper::toDto)
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<?>> subscribeToChannel(UUID subscriberId, ChannelSubscriptionRequestDto dto) {
        if (dto.getChannel() == Channel.PUSH_NOTIFICATION) {
            if (!StringUtils.hasText(dto.getRegId()) || !StringUtils.hasText(dto.getDeviceId())) {
                return Mono.error(new ApplicationException(
                        ExceptionEnum.ERR_1006.getErrorResponseBody("regId and deviceId are required for push notification"),
                        HttpStatus.BAD_REQUEST
                ));
            }
        }

        Mono<Void> updateDeviceInfo = Mono.empty();

        if (dto.getChannel() == Channel.PUSH_NOTIFICATION) {
            updateDeviceInfo = deviceInfoRepository.findByDeviceId(dto.getDeviceId())
                    .defaultIfEmpty(DeviceInfo.builder()
                            .deviceId(dto.getDeviceId())
                            .createdAt(LocalDateTime.now())
                            .build())
                    .flatMap(device -> {
                        device.setRegId(dto.getRegId());
                        device.setModifiedAt(LocalDateTime.now());
                        device.setLastActive(LocalDateTime.now());
                        device.setIsActive(true);
                        return deviceInfoRepository.save(device);
                    })
                    .onErrorResume(e -> {
                        log.error("Exception in subscribeToChannel with subscriberId: {}, and deviceId: {}, Error: {}", subscriberId, dto.getDeviceId(), e.getMessage(), e);
                        return Mono.error(new ApplicationException(
                                ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to subscribe the channel, error: " + e.getMessage()),
                                HttpStatus.INTERNAL_SERVER_ERROR
                        ));
                    })
                    .then();
        }

        Mono<SubscriberChannelPreference> preferenceMono =
                subscriberChannelPreferenceRepository.findBySubscriberIdAndChannel(subscriberId, dto.getChannel())
                        .flatMap(existing -> {
                            existing.setIsSubscribed(dto.getIsSubscribed());
                            existing.setModifiedAt(LocalDateTime.now());
                            return subscriberChannelPreferenceRepository.save(existing);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            SubscriberChannelPreference newPref = SubscriberChannelPreference.builder()
                                    .id(UUID.randomUUID())
                                    .subscriberId(subscriberId)
                                    .channel(dto.getChannel())
                                    .isSubscribed(dto.getIsSubscribed())
                                    .createdAt(LocalDateTime.now())
                                    .modifiedAt(LocalDateTime.now())
                                    .build();
                            return subscriberChannelPreferenceRepository.save(newPref);
                        }))
                        .onErrorResume(e -> {
                            log.error("Exception in creating subscriber preference: {}",e.getMessage(), e);
                            return Mono.error(new ApplicationException(
                                    ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to subscribe the channel, error: " + e.getMessage()),
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            ));
                        });

        return updateDeviceInfo.then(preferenceMono)
                .thenReturn(SuccessEnum.UPDATED.getSuccessResponseBody("Subscription updated"));
    }
}