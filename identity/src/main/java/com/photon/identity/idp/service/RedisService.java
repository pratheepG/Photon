package com.photon.identity.idp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, byte[]> redisTemplateBytes;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate, RedisTemplate<String, byte[]> redisTemplateBytes) {
        this.redisTemplate = redisTemplate;
        this.redisTemplateBytes = redisTemplateBytes;
    }

    public void saveObjectValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getObjectValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void saveByteArrayValue(String key, byte[] value) {
        redisTemplateBytes.opsForValue().set(key, value);
    }

    public byte[] getByteArrayValue(String key) {
        return redisTemplateBytes.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

}