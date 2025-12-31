package com.photon.identity.idp.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.idp.dto.SCAConfigurationDto;
import com.photon.identity.idp.service.SCAConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/sca-configuration")
public class SCAConfigurationController {

    private final SCAConfigurationService scaConfigurationService;

    public SCAConfigurationController(SCAConfigurationService scaConfigurationService) {
        this.scaConfigurationService = scaConfigurationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<SCAConfigurationDto>>> getAll(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.scaConfigurationService.findAll(pageNumber, pageSize));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody SCAConfigurationDto SCAConfigurationDto) {
        return ResponseEntity.ok().body(this.scaConfigurationService.create(SCAConfigurationDto));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<SCAConfigurationDto>> getAuthTypeById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok().body(this.scaConfigurationService.findById(id));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<?>> delete(@RequestParam(value = "ids") List<Long> ids) {
        return ResponseEntity.ok().body(this.scaConfigurationService.delete(ids));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> patch(@PathVariable(value = "id") Long id, @RequestBody SCAConfigurationDto SCAConfigurationDto) {
        return ResponseEntity.ok().body(this.scaConfigurationService.update(id, SCAConfigurationDto));
    }
}