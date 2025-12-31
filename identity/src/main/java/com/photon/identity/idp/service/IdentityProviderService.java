package com.photon.identity.idp.service;

import com.google.common.cache.Cache;
import com.photon.dto.ApiResponseDto;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.entity.Role;
import com.photon.identity.authentication.repository.RoleRepository;
import com.photon.identity.commons.enums.IdentityProviderType;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.mapper.IdentityProviderMapper;
import com.photon.identity.idp.dto.request.IdentityProviderRequestDto;
import com.photon.identity.idp.entity.*;
import com.photon.identity.idp.repository.AuthTypeRepository;
import com.photon.identity.idp.repository.CertificateRepository;
import com.photon.identity.idp.repository.IdentityProviderRepository;
import com.photon.identity.idp.repository.MFAConditionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class IdentityProviderService {

    private final IdentityProviderRepository identityProviderRepository;
    private final AuthTypeRepository authTypeRepository;
    private final CertificateRepository certificateRepository;
    private final MFAConditionRepository mfaConditionRepository;
    private final RoleRepository roleRepository;
    private final Cache<String, Object> cache;

    public IdentityProviderService(IdentityProviderRepository identityProviderRepository, AuthTypeRepository authTypeRepository, CertificateRepository certificateRepository, MFAConditionRepository mfaConditionRepository, RoleRepository roleRepository, Cache<String, Object> cache) {
        this.identityProviderRepository = identityProviderRepository;
        this.authTypeRepository = authTypeRepository;
        this.certificateRepository = certificateRepository;
        this.mfaConditionRepository = mfaConditionRepository;
        this.roleRepository = roleRepository;
        this.cache = cache;
    }

    public ApiResponseDto<List<IdentityProviderDto>> getAll(int pageNumber, int pageSize) throws ApplicationException {
        try {
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<IdentityProviderDto> authTypes = this.identityProviderRepository.findAllIdentityProviders(pageable);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(authTypes.stream().toList()).page(pageable.getPageNumber())
                    .size(authTypes.stream().toList().size()).totalPages(authTypes.getTotalPages())
                    .totalRecords((int) authTypes.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllIdentityProvider: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAllIdentityProvider: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<IdentityProviderDto>> getAllOnboardingIdp(int pageNumber, int pageSize) throws ApplicationException {
        try {
            Pageable pageable = PageRequest.of(pageNumber,pageSize);
            Page<IdentityProviderDto> authTypes = this.identityProviderRepository.findAllOnboardingIdentityProviders(pageable, IdentityProviderType.ON_BOARDING);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(authTypes.stream().toList()).page(pageable.getPageNumber())
                    .size(authTypes.stream().toList().size()).totalPages(authTypes.getTotalPages())
                    .totalRecords((int) authTypes.getTotalElements());
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllOnboardingIdp: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAllOnboardingIdp: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<IdentityProviderDto> getById(String id) {
        try {
            return this.identityProviderRepository.findById(id)
                    .map(identityProvider -> SuccessEnum.SUCCESS.getSuccessResponseBody(IdentityProviderMapper.toDto(identityProvider)))
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Identity Provider not found for the given Id"), HttpStatus.OK));
        } catch (ApplicationException ae) {
            log.error("ApplicationException in getIdentityProviderById: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getIdentityProviderById: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> delete(List<String> ids) throws ApplicationException {
        try {
            this.identityProviderRepository.deleteAllById(ids);
            if(!this.identityProviderRepository.findAllById(ids).isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1011.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
            return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in delete IdentityProvider: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in delete IdentityProvider: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> create(IdentityProviderRequestDto identityProviderDto) {
        try {
            IdentityProvider identityProvider = IdentityProviderMapper.toEntity(identityProviderDto);

            if(Objects.nonNull(identityProviderDto.getCertificate())) {
                Certificate certificate = this.certificateRepository.findById(identityProviderDto.getCertificate())
                        .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1005.getErrorResponseBody(), HttpStatus.OK));
                identityProvider.setCertificate(certificate);
            }

            if(Objects.nonNull(identityProviderDto.getMfaCondition())) {
                MFAConditionSet mfaConditionSet = this.mfaConditionRepository.findById(identityProviderDto.getMfaCondition())
                        .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.OK));
                identityProvider.setMfaCondition(mfaConditionSet);
            }

            if (Objects.nonNull(identityProviderDto.getIdentityProviderAuthTypes())) {
                Set<IdentityProviderAuthType> identityProviderAuthTypes = new LinkedHashSet<>();
                identityProviderDto.getIdentityProviderAuthTypes().forEach(identityProviderAuthType -> {
                    IdentityProviderAuthType idpAuthType = new IdentityProviderAuthType();
                    Optional<AuthType> authType = this.authTypeRepository.findById(identityProviderAuthType.getFirstFactor());

                    if (authType.isEmpty())
                        throw new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("AuthType not found for the given Id"), HttpStatus.OK);

                    if(!identityProviderAuthType.getSecondFactors().isEmpty()) {
                        Set<AuthType> sfAuthTypes = new HashSet<>(this.authTypeRepository.findAllById(identityProviderAuthType.getSecondFactors()));
                        idpAuthType.setSecondFactors(sfAuthTypes);
                    }

                    idpAuthType.setName(identityProviderAuthType.getName());
                    idpAuthType.setActive(identityProviderAuthType.isActive());
                    idpAuthType.setFirstFactor(authType.get());
                    idpAuthType.setIdentityProvider(identityProvider);

                    identityProviderAuthTypes.add(idpAuthType);
                });

                if(!identityProviderAuthTypes.isEmpty()) {
                    identityProviderAuthTypes.forEach(idpAuthType -> {
                        String validationMsg = idpAuthType.validate();
                        if(!StringUtils.isEmpty(validationMsg))
                            throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("First factor auth type: "+idpAuthType.getFirstFactor().getId()+" should't be duplicated in second factor"), HttpStatus.OK);
                    });
                    identityProvider.setIdentityProviderAuthTypes(identityProviderAuthTypes);
                }
            } else {
                throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("IDP Auth_Types cannot be empty"), HttpStatus.OK);
            }

            Role role = new Role();
            role.setAccessLevel(AccessLevel.OWNER);
            role.setIdp(identityProvider.getId());
            role.setRoleId(identityProvider.getId().concat("_DEFAULT_ROLE"));
            role.setName(identityProvider.getId().concat("_DEFAULT_ROLE"));
            role.setDescription("Default role for the IDP: ".concat(identityProvider.getId()));
            role.setActive(true);
            Role savedRol = this.roleRepository.save(role);

            identityProvider.setIdpDefaultRole(savedRol.getId());
            IdentityProvider savedIdentityProvider = this.identityProviderRepository.save(identityProvider);

            if (savedIdentityProvider.getId() == null)
                throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to create the IdentityProvider"), HttpStatus.OK);

            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (ApplicationException ae) {
            log.error("ApplicationException in createIdentityProvider: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in createIdentityProvider: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDto<?> patch(String id, IdentityProviderRequestDto identityProviderDto) {
        try {
            IdentityProvider identityProvider = this.identityProviderRepository.findById(id)
                    .map(idp-> IdentityProviderMapper.partialUpdate(identityProviderDto, idp))
                    .orElseThrow(()->new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody("Identity Provider not found for the given Id"), HttpStatus.OK));

            if(Objects.nonNull(identityProviderDto.getCertificate())) {
                Certificate certificate = this.certificateRepository.findById(identityProviderDto.getCertificate())
                        .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1005.getErrorResponseBody(), HttpStatus.OK));
                identityProvider.setCertificate(certificate);
            }

            if(Objects.nonNull(identityProviderDto.getMfaCondition())) {
                MFAConditionSet mfaConditionSet = this.mfaConditionRepository.findById(identityProviderDto.getMfaCondition())
                        .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.OK));
                identityProvider.setMfaCondition(mfaConditionSet);
            }


            if (Objects.nonNull(identityProviderDto.getIdentityProviderAuthTypes())) {
                Set<IdentityProviderAuthType> identityProviderAuthTypes = new LinkedHashSet<>();
                identityProviderDto.getIdentityProviderAuthTypes().forEach(identityProviderAuthType -> {
                    IdentityProviderAuthType idpAuthType = new IdentityProviderAuthType();
                    Optional<AuthType> authType = this.authTypeRepository.findById(identityProviderAuthType.getFirstFactor());

                    if (authType.isEmpty())
                        throw new ApplicationException(ExceptionEnum.ERR_1017.getErrorResponseBody("AuthType not found for the given Id"), HttpStatus.OK);

                    if(!identityProviderAuthType.getSecondFactors().isEmpty()) {
                        Set<AuthType> sfAuthTypes = new HashSet<>(this.authTypeRepository.findAllById(identityProviderAuthType.getSecondFactors()));
                        idpAuthType.setSecondFactors(sfAuthTypes);
                    }

                    idpAuthType.setName(identityProviderAuthType.getName());
                    idpAuthType.setActive(identityProviderAuthType.isActive());
                    idpAuthType.setFirstFactor(authType.get());
                    idpAuthType.setIdentityProvider(identityProvider);

                    identityProviderAuthTypes.add(idpAuthType);
                });

                if(!identityProviderAuthTypes.isEmpty()) {
                    identityProviderAuthTypes.forEach(idpAuthType -> {
                        String validationMsg = idpAuthType.validate();
                        if(!StringUtils.isEmpty(validationMsg))
                            throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("First factor auth type: "+idpAuthType.getFirstFactor().getId()+" should't be duplicated in second factor"), HttpStatus.OK);
                    });

                    identityProvider.getIdentityProviderAuthTypes().clear();
                    identityProvider.getIdentityProviderAuthTypes().addAll(identityProviderAuthTypes);
                }
            } else {
                throw new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody("IDP Auth_Types cannot be empty"), HttpStatus.OK);
            }

            IdentityProvider savedIdentityProvider = this.identityProviderRepository.save(identityProvider);

            if (savedIdentityProvider.getId() == null)
                throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to create the IdentityProvider"), HttpStatus.OK);

            return SuccessEnum.CREATED.getSuccessResponseBody();

        } catch (ApplicationException ae) {
            log.error("ApplicationException in patch IdentityProvider: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in patch IdentityProvider: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}