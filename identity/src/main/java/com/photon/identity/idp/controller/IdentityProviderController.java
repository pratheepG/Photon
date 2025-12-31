package com.photon.identity.idp.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.request.IdentityProviderRequestDto;
import com.photon.identity.idp.service.IdentityProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/identity-provider")
public class IdentityProviderController {

    private final IdentityProviderService identityProviderService;

    @Autowired
    public IdentityProviderController(IdentityProviderService identityProviderService) {
        this.identityProviderService = identityProviderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<IdentityProviderDto>>> getAll(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.identityProviderService.getAll(pageNumber, pageSize));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody IdentityProviderRequestDto identityProviderDto) {
        return ResponseEntity.ok().body(this.identityProviderService.create(identityProviderDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteAllByIds(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(this.identityProviderService.delete(List.of(id)));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<IdentityProviderDto>> getById(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(this.identityProviderService.getById(id));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<ApiResponseDto<?>> patch(@PathVariable(value = "id") String id, @RequestBody IdentityProviderRequestDto identityProviderDto) {
        return ResponseEntity.ok().body(this.identityProviderService.patch(id, identityProviderDto));
    }

    @GetMapping("/get-all-onboarding-idp")
    public ResponseEntity<ApiResponseDto<List<IdentityProviderDto>>> getAllOnboardingIdp(@RequestParam(required = false) int pageNumber, int pageSize) {
        return ResponseEntity.ok().body(this.identityProviderService.getAllOnboardingIdp(pageNumber, pageSize));
    }
}