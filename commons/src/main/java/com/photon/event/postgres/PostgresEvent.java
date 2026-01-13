package com.photon.event.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import com.photon.event.EventManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CompletableFuture;

public class PostgresEvent implements EventManager {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PostgresEvent(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return CompletableFuture.runAsync(() -> publish(topic, message));
    }

    @Override
    public void publish(String topic, Object message) {
        jdbcTemplate.update("""
            INSERT INTO event_queue (topic, payload)
            VALUES (?, ?::jsonb)
        """, topic, toJson(message));
    }

    @Override
    public <T> EventListener<T> createListener(EventListenerConfig<T> config) {
        return new PostgresEventListener<>(jdbcTemplate, objectMapper, config);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
