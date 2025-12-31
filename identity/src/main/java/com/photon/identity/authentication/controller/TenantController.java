package com.photon.identity.authentication.controller;

import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.TenantDto;
import com.photon.identity.authentication.dto.request.TenantRequestDto;
import com.photon.identity.authentication.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tenants")
@FeatureInfo(id = "TENANT", name = "Tenets", description = "Organisation Tenets")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @ActionInfo(id = "CREATE_TENANT", name = "CREATE_TENANT", accessLevel = AccessLevel.ADMIN, description = "Create the tenant from back-office/admin-panel")
    public ResponseEntity<ApiResponseDto<TenantDto>> createTenant(@RequestBody TenantRequestDto request) throws ApplicationException {
        return new ResponseEntity<>(tenantService.createTenant(request), HttpStatus.CREATED);
    }

    @GetMapping("/{tenantId}")
    @ActionInfo(id = "GET_TENANT_BY_ID", name = "GET_TENANT_BY_ID", accessLevel = AccessLevel.VIEWER, description = "Get tenant details by Id")
    public ResponseEntity<ApiResponseDto<TenantDto>> getTenantById(@PathVariable UUID tenantId) throws ApplicationException {
        return new ResponseEntity<>(tenantService.getTenantById(tenantId), HttpStatus.OK);
    }

    @GetMapping
    @ActionInfo(id = "GET_ALL_TENANT_BY_PAGE", name = "GET_ALL_TENANT_BY_PAGE", accessLevel = AccessLevel.VIEWER, description = "Get all tenant by page")
    public ResponseEntity<ApiResponseDto<List<TenantDto>>> getAllTenants(@RequestParam(required = false) int pageNumber, int pageSize) {
        return new ResponseEntity<>(tenantService.getAllTenants(pageNumber, pageSize), HttpStatus.OK);
    }

}