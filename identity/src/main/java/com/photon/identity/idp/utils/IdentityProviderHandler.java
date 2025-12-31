package com.photon.identity.idp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.idp.dto.mapper.IdentityProviderMapper;
import com.photon.identity.idp.repository.IdentityProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
public class IdentityProviderHandler {
    private final IdentityProviderRepository identityProviderRepository;
    private final Cache<String, Object> cache;
    private final ObjectMapper objectMapper;

    IdentityProviderHandler(IdentityProviderRepository identityProviderRepository, Cache<String, Object> cache, ObjectMapper objectMapper) {
        this.identityProviderRepository = identityProviderRepository;
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    public IdentityProviderDto getIdentityProvider(String provider) throws ApplicationException {
        IdentityProviderDto identityProviderDto;

        Object cachedObject = this.cache.asMap().get(provider);
        if(cachedObject != null){
            try {
                if (cachedObject instanceof String) {
                    String decodedJson = new String(Base64.getDecoder().decode((String) cachedObject));
                    identityProviderDto = objectMapper.readValue(decodedJson, IdentityProviderDto.class);
                } else {
                    identityProviderDto = objectMapper.convertValue(cachedObject, IdentityProviderDto.class);
                }
            } catch (IllegalArgumentException e) {
                throw new ApplicationException(ExceptionEnum.ERR_1004.getErrorResponseBody("Failed to deserialize cached data"), HttpStatus.UNAUTHORIZED);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            identityProviderDto = this.identityProviderRepository.findById(provider)
                    .map(IdentityProviderMapper::toDto)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.UNAUTHORIZED));
            //this.cache.asMap().putIfAbsent(provider, identityProviderDto.toString());
        }

        return identityProviderDto;
    }
}