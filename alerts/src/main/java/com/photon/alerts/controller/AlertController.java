package com.photon.alerts.controller;

import com.photon.alerts.dto.request.AlertRequestDto;
import com.photon.alerts.dto.response.AlertGroupedResponseDto;
import com.photon.alerts.dto.response.AlertResponseDto;
import com.photon.alerts.service.AlertService;
import com.photon.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponseDto<AlertResponseDto>>> createAlert(@RequestBody AlertRequestDto request) {
        return alertService.createAlert(request).map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<AlertResponseDto>>> getAlertById(@PathVariable Long id) {
        return alertService.getById(id).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponseDto<List<AlertGroupedResponseDto>>>> getAllAlerts(@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize) {
        return alertService.getAll(pageNumber, pageSize).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<AlertResponseDto>>> updateAlert(@PathVariable Long id, @RequestBody AlertRequestDto request) {
        return alertService.updateAlert(id, request).map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<AlertResponseDto>>> patchAlert(@PathVariable Long id, @RequestBody AlertRequestDto patchRequest) {
        return alertService.patchAlert(id, patchRequest).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponseDto<?>>> deleteAlert(@PathVariable Long id) {
        return alertService.deleteAlert(id).map(response -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(response));
    }
}