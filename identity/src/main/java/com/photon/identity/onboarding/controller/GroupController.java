package com.photon.identity.onboarding.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.onboarding.dto.request.GroupRequestDto;
import com.photon.identity.onboarding.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody GroupRequestDto dto) {
        return ResponseEntity.ok(groupService.create(dto));
    }
}