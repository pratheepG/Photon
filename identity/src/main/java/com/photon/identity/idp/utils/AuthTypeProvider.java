package com.photon.identity.idp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.idp.dto.AuthTypeDto;
import com.photon.identity.idp.dto.mapper.AuthTypeMapper;
import com.photon.identity.idp.repository.AuthTypeRepository;
import com.photon.identity.idp.service.RedisService;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthTypeProvider {

    private final AuthTypeRepository authTypeRepository;
    private final RedisService redisService;
    private final GenericJackson2JsonRedisSerializer serializer;
    private final ObjectMapper objectMapper;

    AuthTypeProvider(AuthTypeRepository authTypeRepository, RedisService redisService, ObjectMapper objectMapper) {
        this.authTypeRepository = authTypeRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    public AuthTypeDto getAuthType(String authType) throws ApplicationException {
        AuthTypeDto authTypeDto;

        Object cachedObject = this.redisService.getObjectValue(authType);
        if(cachedObject != null){
            try {
                if (cachedObject instanceof String) {
                    String decodedJson = new String(Base64.getDecoder().decode((String) cachedObject));
                    authTypeDto = objectMapper.readValue(decodedJson, AuthTypeDto.class);
                } else {
                    authTypeDto = objectMapper.convertValue(cachedObject, AuthTypeDto.class);
                }
            } catch (IllegalArgumentException e) {
                throw new ApplicationException(ExceptionEnum.ERR_1004.getErrorResponseBody("Failed to deserialize cached data"), HttpStatus.UNAUTHORIZED);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            authTypeDto = this.authTypeRepository.findById(authType)
                    .map(AuthTypeMapper::toDto)
                    .orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1003.getErrorResponseBody(), HttpStatus.UNAUTHORIZED));
            this.redisService.saveObjectValue(authType, this.serializer.serialize(authTypeDto));
        }

        return authTypeDto;
    }

}