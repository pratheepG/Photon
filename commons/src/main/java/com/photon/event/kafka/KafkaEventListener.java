package com.photon.event.kafka;

import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Slf4j
public class KafkaEventListener<T> implements EventListener<T> {

    private final ReceiverOptions<String, Object> receiverOptions;
    private final EventListenerConfig<T> config;
    private Disposable subscription;

    public KafkaEventListener(ReceiverOptions<String, Object> receiverOptions,
                              EventListenerConfig<T> config) {
        this.receiverOptions = receiverOptions;
        this.config = config;
    }

    @Override
    public void start() {
        KafkaReceiver<String, Object> receiver = KafkaReceiver.create(receiverOptions.subscription(config.getTopics()));

        subscription = receiver.receive()
                .doOnNext(record -> {
                    T payload = config.getPayloadType().cast(record.value());
                    config.getHandler().accept(payload);
                    record.receiverOffset().acknowledge();
                })
                .doOnError(e -> log.error("Kafka listener error", e))
                .subscribe();

        log.info("Kafka listener started for topics {}", config.getTopics());
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Kafka listener stopped");
        }
    }
}
