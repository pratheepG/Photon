package com.photon.event.kafka;

import com.photon.event.EventManager;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.concurrent.CompletableFuture;

@Slf4j
public final class KafkaEventReactive implements EventManager {
    private final KafkaSender<String, Object> kafkaSender;

    public KafkaEventReactive(KafkaSender<String, Object> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        log.info("Publishing message to Kafka topic {} reactively", topic);

        return kafkaSender.send(Mono.just(SenderRecord.create(topic, 0, System.currentTimeMillis(), null, message, null)))
                .then()
                .toFuture();
    }

    @Override
    public void publish(String topic, Object message) {
        log.warn("Reactive Kafka does not support blocking publish. Use publishAsync().");
    }
}