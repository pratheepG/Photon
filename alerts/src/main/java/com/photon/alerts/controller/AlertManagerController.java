package com.photon.alerts.controller;

import com.photon.alerts.dto.request.AlertRequestDto;
import com.photon.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/user-alerts")
public class AlertManagerController {

//    private final AlertManagerService alertManagerService;
//
//    public AlertManagerController(AlertManagerService alertManagerService) {
//        this.alertManagerService = alertManagerService;
//    }

//    @PostMapping("/send")
//    public Mono<ResponseEntity<ApiResponseDto<?>>> sendAlert(@RequestBody AlertRequestDto alertRequest) {
//        return this.alertManagerService.sendAlert(alertRequest).map(ResponseEntity.ok()::body);
//    }
}