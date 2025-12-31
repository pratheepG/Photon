package com.photon.identity.idp.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.MFAConditionSetDto;
import com.photon.identity.idp.dto.mapper.IdentityProviderMapper;
import com.photon.identity.idp.entity.MFAConditionSet;
import com.photon.identity.idp.repository.MFAConditionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MFAConditionService {

    private final MFAConditionRepository mfaConditionRepository;

    public MFAConditionService(MFAConditionRepository mfaConditionRepository) {
        this.mfaConditionRepository = mfaConditionRepository;
    }

    @Transactional
    public ApiResponseDto<?> create(MFAConditionSetDto mfaConditionSetDto) {
        try {
            MFAConditionSet mfaConditionSet = this.mfaConditionRepository.save(IdentityProviderMapper.toEntity(mfaConditionSetDto));
            if (mfaConditionSet.getId() == null)
                throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to create the MFACondition"), HttpStatus.OK);
            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in create MFACondition : {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in create MFACondition : {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<MFAConditionSetDto>> getAll(int pageNumber, int pageSize) throws ApplicationException {
        try {
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<MFAConditionSetDto> mfaCondition = this.mfaConditionRepository.findAll(pageable).map(IdentityProviderMapper::toDto);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(mfaCondition.stream().toList(), null,
                    pageable.getPageNumber(), mfaCondition.stream().toList().size(),
                    mfaCondition.getTotalPages(), (int) mfaCondition.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAll: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAll: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}