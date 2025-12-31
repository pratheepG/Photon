package com.photon.event.kafka;

import com.photon.event.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
final public class KafkaEvent implements EventManager {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEvent(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return CompletableFuture.runAsync(() -> kafkaTemplate.send(topic, message));
    }

    @Override
    public void publish(String topic, Object message) {

    }
}