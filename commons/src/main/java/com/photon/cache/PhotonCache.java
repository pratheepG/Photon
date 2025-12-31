package com.photon.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PhotonCache {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    public Object get(String key) {
        return store.get(key);
    }

    public void put(String key, String value) {
        store.put(key, value);
    }

    public void putIfAbsent(String key, Object value) {
        store.putIfAbsent(key, value);
    }

    public void remove(String key) {
        store.remove(key);
    }

    public Map<String, Object> getAll() {
        return new ConcurrentHashMap<>(store);
    }

    public void update(Map<String, Object> newConfig) {
        store.putAll(newConfig);
    }

}