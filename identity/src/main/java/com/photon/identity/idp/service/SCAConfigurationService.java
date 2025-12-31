package com.photon.identity.idp.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.SCAConfigurationDto;
import com.photon.identity.idp.dto.mapper.SCAConfigurationMapper;
import com.photon.identity.idp.entity.AuthType;
import com.photon.identity.idp.entity.SCAConfiguration;
import com.photon.identity.idp.repository.AuthTypeRepository;
import com.photon.identity.idp.repository.SCAConfigurationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SCAConfigurationService {

    private final SCAConfigurationRepository scaConfigurationRepository;
    private final AuthTypeRepository authTypeRepository;

    public SCAConfigurationService(SCAConfigurationRepository configurationRepository, AuthTypeRepository authTypeRepository) {
        this.scaConfigurationRepository = configurationRepository;
        this.authTypeRepository = authTypeRepository;
    }

    @Transactional
    public ApiResponseDto<?> create(SCAConfigurationDto SCAConfigurationDto) throws ApplicationException {
        try {
            log.debug("create input param : {}", SCAConfigurationDto.toString());
            if(Objects.nonNull(SCAConfigurationDto.getId()))
                SCAConfigurationDto.setId(null);

            AuthType firstFactor = null;
            if (Objects.nonNull(SCAConfigurationDto.getFirstFactor())) {
                firstFactor = this.authTypeRepository.findById(SCAConfigurationDto.getFirstFactor())
                        .orElseThrow(() -> new ApplicationException(
                                ExceptionEnum.ERR_1006.getErrorResponseBody("First factor Auth Type not found"), HttpStatus.NOT_FOUND));
            }

            Set<AuthType> secondFactors = null;
            if (Objects.nonNull(SCAConfigurationDto.getSecondFactors()) && !SCAConfigurationDto.getSecondFactors().isEmpty()) {
                secondFactors = new HashSet<>(this.authTypeRepository.findAllById(SCAConfigurationDto.getSecondFactors()));

                if (secondFactors.size() != SCAConfigurationDto.getSecondFactors().size()) {
                    throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("One or more second factor Auth Types not found"),
                            HttpStatus.NOT_FOUND);
                }
            }

            SCAConfiguration mfaConfiguration = this.scaConfigurationRepository.save(SCAConfigurationMapper.toEntity(SCAConfigurationDto, firstFactor, secondFactors));

            if (Objects.nonNull(mfaConfiguration.getId()))
                return SuccessEnum.CREATED.getSuccessResponseBody();
            else
                throw new ApplicationException(ExceptionEnum.ERR_1010.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in create new MFAConfiguration: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in create new MFAConfiguration: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> update(Long id, SCAConfigurationDto SCAConfigurationDto) throws ApplicationException {
        try {
            log.debug("update input param: {}, id: {}", SCAConfigurationDto, id);
            SCAConfigurationDto.setId(null);

            SCAConfiguration mfaConfigurationExistingRecord = this.scaConfigurationRepository.findById(id)
                    .orElseThrow(() -> new ApplicationException(
                            ExceptionEnum.ERR_1006.getErrorResponseBody("MFA Configuration not found"), HttpStatus.NOT_FOUND));

            AuthType firstFactor = null;
            if (Objects.nonNull(SCAConfigurationDto.getFirstFactor())) {
                firstFactor = this.authTypeRepository.findById(SCAConfigurationDto.getFirstFactor())
                        .orElseThrow(() -> new ApplicationException(
                                ExceptionEnum.ERR_1006.getErrorResponseBody("First factor Auth Type not found"), HttpStatus.NOT_FOUND));
            }

            Set<AuthType> secondFactors = null;
            if (Objects.nonNull(SCAConfigurationDto.getSecondFactors()) && !SCAConfigurationDto.getSecondFactors().isEmpty()) {
                secondFactors = new HashSet<>(this.authTypeRepository.findAllById(SCAConfigurationDto.getSecondFactors()));

                if (secondFactors.size() != SCAConfigurationDto.getSecondFactors().size()) {
                    throw new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("One or more second factor Auth Types not found"),
                            HttpStatus.NOT_FOUND);
                }
            }

            SCAConfiguration updatedRecord = SCAConfigurationMapper.partialUpdate(SCAConfigurationDto, mfaConfigurationExistingRecord, firstFactor, secondFactors);
            SCAConfiguration savedRecord = this.scaConfigurationRepository.save(updatedRecord);

            if (savedRecord.getId() != null) {
                return SuccessEnum.UPDATED.getSuccessResponseBody();
            }

            throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
        } catch (ApplicationException ae) {
            log.error("ApplicationException in update MFAConfiguration: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e) {
            log.error("Exception in update MFAConfiguration: {}", e.getMessage(), e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<SCAConfigurationDto>> findAll(int pageNumber, int pageSize) throws ApplicationException {
        try {
            log.debug("get All input param pageNumber: {}, pageSize: {}", pageNumber, pageSize);
            Pageable pageable = PageRequest.of(pageNumber,pageSize);

            Page<SCAConfigurationDto> mfaConfiguration = this.scaConfigurationRepository.findAll(pageable)
                    .map(SCAConfigurationMapper::toDto);

            return SuccessEnum.SUCCESS.getSuccessResponseBody(mfaConfiguration.stream().toList(), null,
                    pageable.getPageNumber(), mfaConfiguration.stream().toList().size(),
                    mfaConfiguration.getTotalPages(), (int) mfaConfiguration.getTotalElements());

        } catch (ApplicationException ae) {
            log.error("ApplicationException in get All MFAConfiguration: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in get All MFAConfiguration: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<SCAConfigurationDto> findById(Long id) throws ApplicationException {
        try {
            log.debug("get All input param id: {}", id);

            SCAConfigurationDto mfaConfiguration = this.scaConfigurationRepository.findById(id)
                    .map(SCAConfigurationMapper::toDto).orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));

            return SuccessEnum.SUCCESS.getSuccessResponseBody(mfaConfiguration);

        } catch (ApplicationException ae) {
            log.error("ApplicationException in findById MFAConfiguration: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in findById MFAConfiguration: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> delete(List<Long> ids) throws ApplicationException {
        try {
            log.debug("get delete input param ids: {}", ids);
            this.scaConfigurationRepository.deleteAllById(ids);
            if(!this.scaConfigurationRepository.findAllById(ids).isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1011.getErrorResponseBody(), HttpStatus.BAD_REQUEST);

            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in delete MFAConfiguration: {}", ae.getMessage(), ae);
            throw ae;
        } catch (Exception e){
            log.error("Exception in delete MFAConfiguration: {} ",e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}