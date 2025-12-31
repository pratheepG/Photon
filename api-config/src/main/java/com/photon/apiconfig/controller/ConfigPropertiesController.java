package com.photon.apiconfig.controller;

import com.photon.apiconfig.dto.ConfigPropertiesDto;
import com.photon.apiconfig.service.ConfigPropertiesService;
import com.photon.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config-properties")
public class ConfigPropertiesController {

    private final ConfigPropertiesService configService;

    public ConfigPropertiesController(ConfigPropertiesService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<ConfigPropertiesDto>> getConfigByApplicationId(@RequestParam String applicationId) {
        return ResponseEntity.ok(this.configService.getConfigForApplication(applicationId));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createConfig(@RequestBody ConfigPropertiesDto configDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.configService.createConfig(configDto));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> patchConfig(@PathVariable(value = "id") String id, @RequestBody ConfigPropertiesDto configDto) {
        return ResponseEntity.status(HttpStatus.OK).body(this.configService.patchConfig(configDto, id));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> updateConfig(@PathVariable(value = "id") String id, @RequestBody ConfigPropertiesDto configDto) {
        return ResponseEntity.status(HttpStatus.OK).body(this.configService.updateConfig(configDto, id));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<?>> deleteConfig(@RequestParam String applicationId) {
        return ResponseEntity.status(HttpStatus.OK).body(this.configService.deleteConfig(applicationId));
    }
}