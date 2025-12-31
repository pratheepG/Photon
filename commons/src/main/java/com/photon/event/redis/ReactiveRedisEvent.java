package com.photon.event.redis;

import com.photon.event.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(name = "org.springframework.data.redis.core.ReactiveRedisTemplate")
public class ReactiveRedisEvent implements EventManager {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Map<String, Disposable> activeSubscriptions = new ConcurrentHashMap<>();

    public ReactiveRedisEvent(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("✅ Reactive RedisEvent initialized successfully");
    }

    public Mono<Long> publish(ChannelTopic topic, Object message) {
        return redisTemplate.convertAndSend(topic.getTopic(), message)
                .doOnNext(result -> log.debug("📤 Published message to topic: {}", topic.getTopic()))
                .doOnError(e -> log.error("❌ Failed to publish message to topic: {}", topic.getTopic(), e));
    }

    public void subscribe(ChannelTopic topic, java.util.function.Consumer<ReactiveSubscription.Message<String, Object>> listener) {
        if (activeSubscriptions.containsKey(topic.getTopic())) {
            log.warn("🔁 Already subscribed to topic: {}", topic.getTopic());
            return;
        }

        Disposable subscription = redisTemplate.listenTo(topic)
                .doOnNext(message -> {
                    log.info("📩 Received message from topic: {}", message.getChannel());
                    listener.accept(message);
                })
                .doOnError(e -> log.error("❌ Error while listening to topic: {}", topic.getTopic(), e))
                .subscribe();

        activeSubscriptions.put(topic.getTopic(), subscription);
    }

    public void unsubscribe(ChannelTopic topic) {
        Disposable subscription = activeSubscriptions.remove(topic.getTopic());
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("🛑 Unsubscribed from topic: {}", topic.getTopic());
        } else {
            log.warn("⚠️ Tried to unsubscribe from non-existent or already disposed topic: {}", topic.getTopic());
        }
    }

    @Override
    public void publish(String topic, Object message) {
        redisTemplate.convertAndSend(topic, message)
                .doOnNext(result -> log.debug("📤 Published message to topic: {}", topic))
                .doOnError(e -> log.error("❌ Failed to publish message to topic: {}", topic, e))
                .subscribe();
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return redisTemplate.convertAndSend(topic, message)
                .doOnNext(r -> log.debug("🚀 Async published message to topic: {}", topic))
                .doOnError(e -> log.error("❌ Async publish failed for topic: {}", topic, e))
                .then()
                .toFuture();
    }
}
