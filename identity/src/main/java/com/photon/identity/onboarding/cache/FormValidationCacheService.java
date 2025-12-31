package com.photon.identity.onboarding.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FormValidationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "scenario-validation:";

    public void saveValidationConfig(String scenarioId, Map<String, List<String>> requiredFieldsByForm) {
        redisTemplate.opsForValue().set(PREFIX + scenarioId, requiredFieldsByForm);
    }

    public Map<String, List<String>> getValidationConfig(String scenarioId) {
        Object obj = redisTemplate.opsForValue().get(PREFIX + scenarioId);
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, List<String>>) map;
        }
        return Map.of();
    }

    public void evict(String scenarioId) {
        redisTemplate.delete(PREFIX + scenarioId);
    }
}