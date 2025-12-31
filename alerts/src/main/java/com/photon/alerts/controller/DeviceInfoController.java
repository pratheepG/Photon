package com.photon.alerts.controller;

import com.photon.alerts.dto.request.DeviceInfoRequestDto;
import com.photon.alerts.dto.response.DeviceInfoResponseDto;
import com.photon.alerts.service.DeviceInfoService;
import com.photon.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/device")
public class DeviceInfoController {

    private final DeviceInfoService deviceInfoService;

    public DeviceInfoController(DeviceInfoService deviceInfoService) {
        this.deviceInfoService = deviceInfoService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponseDto<DeviceInfoResponseDto>>> registerDevice(@RequestBody DeviceInfoRequestDto requestDto) {
        return deviceInfoService.registerDevice(requestDto).map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<DeviceInfoResponseDto>>> getDeviceById(@PathVariable UUID id) {
        return deviceInfoService.getById(id).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<DeviceInfoResponseDto>>> updateDevice(@PathVariable UUID id, @RequestBody DeviceInfoRequestDto requestDto) {
        return deviceInfoService.updateDevice(id, requestDto).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<?>>> deleteDevice(@PathVariable UUID id) {
        return deviceInfoService.delete(id).map(ResponseEntity::ok);
    }

    @PostMapping("/link-to-subscriber")
    public Mono<ResponseEntity<ApiResponseDto<?>>> linkDeviceToSubscriber(@RequestParam UUID subscriberId, @RequestParam String deviceId, @RequestParam String userId) {
        return deviceInfoService.linkDeviceToSubscriber(subscriberId, deviceId, userId).map(ResponseEntity::ok);
    }
}