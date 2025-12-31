package com.photon.identity.onboarding.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.onboarding.dto.OnboardingSubmissionRequestDto;
import com.photon.identity.onboarding.service.OnboardingSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onboarding/register-user")
@RequiredArgsConstructor
public class OnboardingSubmissionController {

    private final OnboardingSubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> submitStep(@RequestBody OnboardingSubmissionRequestDto dto) {
        return ResponseEntity.ok(submissionService.saveStep(dto));
    }
}