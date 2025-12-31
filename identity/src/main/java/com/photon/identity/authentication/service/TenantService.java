package com.photon.identity.authentication.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.dto.TenantDto;
import com.photon.identity.authentication.dto.mapper.TenantMapper;
import com.photon.identity.authentication.dto.request.TenantRequestDto;
import com.photon.identity.authentication.entity.Tenant;
import com.photon.identity.authentication.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public ApiResponseDto<TenantDto> createTenant(TenantRequestDto request) throws ApplicationException {
        
        Tenant newTenant = new Tenant();
        newTenant.setName(request.getName());
        newTenant.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            Tenant parent = tenantRepository.findById(UUID.fromString(request.getParentId()))
                .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("Parent Tenant not found with ID: " + request.getParentId()), HttpStatus.BAD_REQUEST));
            newTenant.setParent(parent);
        }

        Tenant savedTenant = tenantRepository.save(newTenant);
        TenantDto responseDto = TenantMapper.toDto(savedTenant);
        return SuccessEnum.CREATED.getSuccessResponseBody(responseDto, "Tenant created successfully.");
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<TenantDto> getTenantById(UUID tenantId) throws ApplicationException {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("Tenant not found with ID: " + tenantId), HttpStatus.NOT_FOUND));
        return SuccessEnum.SUCCESS.getSuccessResponseBody(TenantMapper.toDto(tenant), "Tenant fetched successfully.");
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<List<TenantDto>> getAllTenants(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0,pageNumber), Math.max(1,pageSize));
        Page<Tenant> tenantPage = tenantRepository.findAll(pageable);
        List<TenantDto> tenants = tenantPage.stream().map(TenantMapper::toDto).collect(Collectors.toList());
        return SuccessEnum.SUCCESS.getSuccessResponseBody(tenants, null, pageable.getPageNumber(), tenants.size(), tenantPage.getTotalPages(), (int) tenantPage.getTotalElements());
    }

}