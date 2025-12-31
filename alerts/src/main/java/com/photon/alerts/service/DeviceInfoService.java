package com.photon.alerts.service;

import com.photon.alerts.dto.mapper.DeviceInfoMapper;
import com.photon.alerts.dto.request.DeviceInfoRequestDto;
import com.photon.alerts.dto.response.DeviceInfoResponseDto;
import com.photon.alerts.entity.DeviceInfo;
import com.photon.alerts.entity.SubscriberDeviceMap;
import com.photon.alerts.repository.DeviceInfoRepository;
import com.photon.alerts.repository.SubscriberDeviceMapRepository;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class DeviceInfoService {

    private final DeviceInfoRepository deviceInfoRepository;
    private final SubscriberDeviceMapRepository subscriberDeviceMapRepository;

    public DeviceInfoService(DeviceInfoRepository deviceInfoRepository, SubscriberDeviceMapRepository subscriberDeviceMapRepository) {
        this.deviceInfoRepository = deviceInfoRepository;
        this.subscriberDeviceMapRepository = subscriberDeviceMapRepository;
    }

    public Mono<ApiResponseDto<DeviceInfoResponseDto>> save(DeviceInfo deviceInfo) {
        return deviceInfoRepository.save(deviceInfo)
                .map(DeviceInfoMapper::toDto)
                .map(SuccessEnum.CREATED::getSuccessResponseBody)
                .onErrorResume(e -> {
                    if (e instanceof DataIntegrityViolationException || (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("duplicate key value"))) {
                        log.warn("Duplicate key when saving device : {}", e.getMessage());
                        return deviceInfoRepository.findByDeviceId(deviceInfo.getDeviceId())
                                .map(DeviceInfoMapper::toDto)
                                .map(SuccessEnum.CREATED::getSuccessResponseBody);
                    }
                    log.error("Error in registerDevice: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to register device"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    public Mono<ApiResponseDto<DeviceInfoResponseDto>> registerDevice(DeviceInfoRequestDto requestDto) {
        return this.save(DeviceInfoMapper.toEntity(requestDto));
    }

    public Mono<ApiResponseDto<DeviceInfoResponseDto>> getById(UUID id) {
        return deviceInfoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody("Device not found"), HttpStatus.NOT_FOUND)))
                .map(DeviceInfoMapper::toDto)
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<DeviceInfoResponseDto>> updateDevice(UUID id, DeviceInfoRequestDto updatedDto) {
        return deviceInfoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1001.getErrorResponseBody("Device not found"), HttpStatus.NOT_FOUND)))
                .flatMap(existing -> {
                    DeviceInfo updated = DeviceInfoMapper.toEntity(updatedDto);
                    updated.setId(id);
                    updated.setCreatedAt(existing.getCreatedAt());
                    updated.setModifiedAt(LocalDateTime.now());
                    return deviceInfoRepository.save(updated);
                })
                .map(DeviceInfoMapper::toDto)
                .map(SuccessEnum.UPDATED::getSuccessResponseBody);
    }

    public Mono<ApiResponseDto<?>> delete(UUID id) {
        return deviceInfoRepository.deleteById(id).thenReturn(SuccessEnum.DELETED.getSuccessResponseBody());
    }

    public Mono<ApiResponseDto<?>> linkDeviceToSubscriber(UUID subscriberId, String deviceId, String userId) {
        return deviceInfoRepository.findByDeviceId(deviceId)
                .switchIfEmpty(Mono.error(new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Device not found"), HttpStatus.NOT_FOUND)))
                .map(deviceInfo -> SubscriberDeviceMap.builder()
                        .subscriberId(subscriberId).deviceInfoId(deviceInfo.getId())
                        .userId(userId).isActive(Boolean.TRUE).build())
                .flatMap(o -> subscriberDeviceMapRepository.save(o)
                        .<ApiResponseDto<?>>map(saved -> SuccessEnum.CREATED.getSuccessResponseBody())
                        .onErrorResume(e -> {
                            if (e instanceof DataIntegrityViolationException ||
                                    (e.getCause() != null && e.getCause().getMessage() != null &&
                                            e.getCause().getMessage().contains("duplicate key value"))) {
                                log.warn("Duplicate key when linking device to subscriber: {}", e.getMessage());
                                return Mono.just(SuccessEnum.CREATED.getSuccessResponseBody()); // treat as already linked
                            }
                            log.error("Error linking device to subscriber: {}", e.getMessage(), e);
                            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to link device to subscriber"), HttpStatus.INTERNAL_SERVER_ERROR));
                        }))
                .onErrorResume(e -> {
                    log.error("Error linking device to subscriber: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to link device to subscriber"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

}