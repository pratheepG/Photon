package com.photon.identity.idp.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.service.AuthTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/auth-type")
public class AuthTypeController {

    private final AuthTypeService authTypeService;

    public AuthTypeController(AuthTypeService authTypeService) {
        this.authTypeService = authTypeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AuthTypeDto>>> getAllAuthTypes(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.authTypeService.getAllAuthTypes(pageNumber, pageSize));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createAuthType(@RequestBody AuthTypeDto authTypeDto) {
        return ResponseEntity.ok().body(this.authTypeService.createAuthType(authTypeDto));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<AuthTypeDto>> getAuthTypeById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(this.authTypeService.getAuthTypeById(id));
    }

    @PutMapping
    public ResponseEntity<ApiResponseDto<?>> updateAuthType(@RequestBody AuthTypeDto authTypeDto) {
        return ResponseEntity.ok().body(this.authTypeService.updateAuthType(authTypeDto));
    }
}