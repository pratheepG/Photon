package com.photon.apiconfig.controller;

import com.photon.apiconfig.dto.ConfigPropertiesDto;
import com.photon.apiconfig.service.ConfigPropertiesService;
import com.photon.dto.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/client-config-properties")
public class ClientConfigPropertiesController {

    private final ConfigPropertiesService configService;

    public ClientConfigPropertiesController(ConfigPropertiesService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<ConfigPropertiesDto>> getConfigByApplicationId() {
        return ResponseEntity.ok(this.configService.getConfigForApplication("CLIENT-PROPERTIES"));
    }

}