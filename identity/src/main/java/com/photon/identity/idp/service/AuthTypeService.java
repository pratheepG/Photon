package com.photon.identity.idp.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.mapper.AuthTypeMapper;
import com.photon.identity.idp.entity.AuthType;
import com.photon.identity.idp.repository.AuthTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class AuthTypeService {

    private final AuthTypeRepository authTypeRepository;

    @Autowired
    public AuthTypeService(AuthTypeRepository authTypeRepository) {
        this.authTypeRepository = authTypeRepository;
    }

    public ApiResponseDto<List<AuthTypeDto>> getAllAuthTypes(int pageNumber, int pageSize) throws ApplicationException {
        try {
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<AuthTypeDto> authTypes = authTypeRepository.findAll(pageable).map(AuthTypeMapper::toResponseDto);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(authTypes.stream().toList())
                            .page(pageable.getPageNumber()).size(authTypes.stream().toList().size())
                            .totalPages(authTypes.getTotalPages()).totalRecords((int) authTypes.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllAuthTypes: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAllAuthTypes: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<AuthTypeDto> getAuthTypeById(String id) {
        try {
            return this.authTypeRepository.findById(id)
                    .map(authType -> SuccessEnum.SUCCESS.getSuccessResponseBody(AuthTypeMapper.toDto(authType)))
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("AuthType not found for the given Id"), HttpStatus.OK));
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAuthTypeById: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAuthTypeById: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CacheEvict("meta")
    public ApiResponseDto<?> createAuthType(AuthTypeDto authTypeDto) {
        try {
            AuthType savedAuthType = this.authTypeRepository.save(Objects.requireNonNull(AuthTypeMapper.toEntity(authTypeDto)));
            if (savedAuthType.getId() == null)
                throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to create the AuthType"), HttpStatus.OK);
            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in createAuthType: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in createAuthType: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CacheEvict("meta")
    public ApiResponseDto<?> updateAuthType(AuthTypeDto authTypeDto) {
        try {
            Optional<AuthType> optionalAuthType = this.authTypeRepository.findById(authTypeDto.getId());

            if (optionalAuthType.isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("AuthType not found for the given Id"), HttpStatus.OK);

            if (this.authTypeRepository.save(AuthTypeMapper.partialUpdate(authTypeDto, optionalAuthType.get())).getId() == null)
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody("Failed to update the AuthType"), HttpStatus.OK);
            return SuccessEnum.UPDATED.getSuccessResponseBody();

        } catch (ApplicationException ae) {
            log.error("ApplicationException in updateAuthType: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in updateAuthType: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}