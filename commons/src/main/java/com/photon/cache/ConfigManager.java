package com.photon.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigManager {

    private final Map<String, String> configStore = new ConcurrentHashMap<>();

    public String getConfigValue(String key) {
        return configStore.get(key);
    }

    public void updateConfigValue(String key, String value) {
        configStore.put(key, value);
    }

    public Map<String, String> getAllConfig() {
        return new ConcurrentHashMap<>(configStore);
    }

    public void updateConfig(Map<String, String> newConfig) {
        configStore.putAll(newConfig);
    }

}