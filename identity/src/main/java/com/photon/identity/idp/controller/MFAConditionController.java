package com.photon.identity.idp.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.idp.dto.MFAConditionSetDto;
import com.photon.identity.idp.service.MFAConditionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/mfa-condition")
public class MFAConditionController {

    private final MFAConditionService mfaConditionService;

    public MFAConditionController(MFAConditionService mfaConditionService) {
        this.mfaConditionService = mfaConditionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<MFAConditionSetDto>>> getAll(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.mfaConditionService.getAll(pageNumber, pageSize));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody MFAConditionSetDto mfaConditionSetDto) {
        return ResponseEntity.ok().body(this.mfaConditionService.create(mfaConditionSetDto));
    }

}