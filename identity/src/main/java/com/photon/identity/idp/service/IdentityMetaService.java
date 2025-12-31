package com.photon.identity.idp.service;

import com.photon.constants.ResponseConstant;
import com.photon.dto.ApiResponseDto;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.IdentityProviderType;
import com.photon.identity.onboarding.enums.DateFormat;
import com.photon.identity.onboarding.enums.FieldType;
import com.photon.identity.onboarding.enums.FileFormat;
import com.photon.identity.onboarding.enums.SizeUnit;
import com.photon.identity.commons.properties.OauthConfigProperties;
import com.photon.identity.idp.repository.AuthTypeRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdentityMetaService {

    private final AuthTypeRepository authTypeRepository;
    private final OauthConfigProperties oauthConfigProperties;

    @Autowired
    public IdentityMetaService(AuthTypeRepository authTypeRepository, OauthConfigProperties oauthConfigProperties) {
        this.authTypeRepository = authTypeRepository;
        this.oauthConfigProperties = oauthConfigProperties;
    }

    @Cacheable("meta")
    public ApiResponseDto<Map<String, Object>> getAllMeta(){
        Map<String, Object> meta = new HashMap<>();
        meta.put("authAdaptor", this.getAllAuthAdaptor());
        meta.put("authTypes", this.getAllAuthType());
        meta.put("idpTypes", this.getAllIDPTypes());
        meta.put("jwtSignatureAlgorithms", this.getJwtSignatureAlgorithms());
        meta.put("oauthProviders", this.oauthConfigProperties.getConfig());
        meta.put("formFieldTypes", this.getFormFieldTypes());
        meta.put("fileTypes", this.getFileTypes());
        meta.put("fileSizeUnits", this.getFileSizeUnits());
        meta.put("dateFormates", this.getDateFormat());

        return new ApiResponseDto.builder<Map<String, Object>>()
                .message(ResponseConstant.SUCCESS).opStatus(0).success(true).responseData(meta).build();
    }

    private Map<String, String> getAllAuthAdaptor(){
        return AuthAdaptor.toMap();
    }

    private Set<String> getFormFieldTypes(){
        return Arrays.stream(FieldType.values()).map(FieldType::name).collect(Collectors.toSet());
    }

    private Set<String> getFileSizeUnits(){
        return Arrays.stream(SizeUnit.values()).map(SizeUnit::name).collect(Collectors.toSet());
    }

    private Set<String> getDateFormat(){
        return Arrays.stream(DateFormat.values()).map(DateFormat::name).collect(Collectors.toSet());
    }

    private Map<String, List<String>> getFileTypes(){
        return Arrays.stream(FileFormat.values())
                .collect(Collectors.groupingBy(
                        format -> format.getFileType().name(),
                        Collectors.mapping(Enum::name, Collectors.toList())
                ));
    }

    private Set<Map<String, Object>> getAllAuthType(){
        return this.authTypeRepository.findAll().stream()
                .map(authType->{
                    Map<String, Object> authTypeMap = new HashMap<>();
                    authTypeMap.put("id", authType.getId());
                    authTypeMap.put("name", authType.getName());
                    authTypeMap.put("description", authType.getDescription());
                    authTypeMap.put("isActive", authType.getIsActive());
                    return authTypeMap;
                })
                .collect(Collectors.toSet());
    }

    private Set<IdentityProviderType> getAllIDPTypes() {
        return Arrays.stream(IdentityProviderType.values()).collect(Collectors.toSet());
    }

    private Set<Map<String, String>> getJwtSignatureAlgorithms() {
        return Arrays.stream(SignatureAlgorithm.values())
                .map(alg -> Map.of(
                        "id", alg.name(),
                        "name", alg.getValue(),
                        "description", alg.getDescription(),
                        "familyName", alg.getFamilyName(),
                        "jcaName", String.valueOf(alg.getJcaName()),
                        "jdkStandard", String.valueOf(alg.isJdkStandard())
                ))
                .collect(Collectors.toSet());
    }

}