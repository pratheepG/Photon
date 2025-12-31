package com.photon.identity.authentication.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.authentication.dto.response.UserDeviceRegistrationResponseDto;
import com.photon.identity.authentication.service.UserDeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user-device")
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserDeviceRegistrationResponseDto>> registerDevice() {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userDeviceService.registerDevice());
    }
}