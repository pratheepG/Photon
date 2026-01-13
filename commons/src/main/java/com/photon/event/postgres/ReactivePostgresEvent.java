package com.photon.event.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import com.photon.event.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReactivePostgresEvent implements EventManager {

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    public ReactivePostgresEvent(DatabaseClient databaseClient,
                                 ObjectMapper objectMapper) {
        this.databaseClient = databaseClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, Object message) {
        publishAsync(topic, message);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return databaseClient.sql("""
                INSERT INTO event_queue (topic, payload)
                VALUES (:topic, :payload::jsonb)
                """)
                .bind("topic", topic)
                .bind("payload", toJson(message))
                .then()
                .toFuture();
    }

    @Override
    public <T> EventListener<T> createListener(EventListenerConfig<T> config) {
        return new ReactivePostgresEventListener<>(
                databaseClient,
                objectMapper,
                config
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
