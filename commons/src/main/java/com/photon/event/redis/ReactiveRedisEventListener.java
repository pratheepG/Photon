package com.photon.event.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import reactor.core.Disposable;

@Slf4j
public class ReactiveRedisEventListener<T> implements EventListener<T> {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final EventListenerConfig<T> config;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    public ReactiveRedisEventListener(ReactiveRedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper,
                                      EventListenerConfig<T> config) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Override
    public void start() {
        ChannelTopic[] topics = config.getTopics()
                .stream().map(ChannelTopic::new)
                .toArray(ChannelTopic[]::new);

        subscription = redisTemplate.listenTo(topics)
                .doOnNext(this::handleMessage)
                .doOnError(e -> log.error("❌ Reactive Redis listener error", e))
                .subscribe();

        log.info("⚡ Reactive Redis listener started for topics {}", config.getTopics());
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("🛑 Reactive Redis listener stopped");
        }
    }

    private void handleMessage(ReactiveSubscription.Message<String, Object> message) {
        try {
            T payload = objectMapper.convertValue(
                    message.getMessage(),
                    config.getPayloadType()
            );
            config.getHandler().accept(payload);
        } catch (Exception e) {
            log.error("❌ Failed to process reactive Redis message", e);
        }
    }
}