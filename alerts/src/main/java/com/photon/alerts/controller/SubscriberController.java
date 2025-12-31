package com.photon.alerts.controller;

import com.photon.alerts.dto.request.ChannelSubscriptionRequestDto;
import com.photon.dto.ApiResponseDto;
import com.photon.alerts.dto.request.SubscriberRequestDto;
import com.photon.alerts.dto.response.SubscriberResponseDto;
import com.photon.alerts.service.SubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/subscribers")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponseDto<SubscriberResponseDto>>> create(@RequestBody SubscriberRequestDto request) {
        return subscriberService.create(request).map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<SubscriberResponseDto>>> getById(@PathVariable UUID id) {
        return subscriberService.getById(id).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<SubscriberResponseDto>>> update(@PathVariable UUID id, @RequestBody SubscriberRequestDto request) {
        return subscriberService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<?>>> delete(@PathVariable UUID id) {
        return subscriberService.delete(id).map(ResponseEntity::ok);
    }

    @GetMapping("/by-user-id/{userId}")
    public Mono<ResponseEntity<ApiResponseDto<SubscriberResponseDto>>> findByUserId(@PathVariable String userId) {
        return subscriberService.findByUserId(userId).map(ResponseEntity::ok);
    }

    @GetMapping("/by-unique-id/{uniqueId}")
    public Mono<ResponseEntity<ApiResponseDto<SubscriberResponseDto>>> findByUniqueId(@PathVariable String uniqueId) {
        return subscriberService.findByUniqueId(uniqueId).map(ResponseEntity::ok);
    }

    @PostMapping("/{subscriberId}/channel-subscription")
    public Mono<ResponseEntity<ApiResponseDto<?>>> subscribeToChannel(@PathVariable UUID subscriberId, @RequestBody ChannelSubscriptionRequestDto dto) {
        return subscriberService.subscribeToChannel(subscriberId, dto).map(ResponseEntity::ok);
    }

}