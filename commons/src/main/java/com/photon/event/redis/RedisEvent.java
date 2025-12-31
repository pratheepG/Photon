package com.photon.event.redis;

import com.photon.event.EventManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisEvent implements EventManager {

    private final RedisMessageListenerContainer listenerContainer;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisEvent(RedisMessageListenerContainer listenerContainer,
                      RedisTemplate<String, Object> redisTemplate) {
        this.listenerContainer = listenerContainer;
        this.redisTemplate = redisTemplate;
        log.info("RedisEvent initialized successfully");
    }

    public void publish(ChannelTopic topic, Object message) {
        try {
            redisTemplate.convertAndSend(topic.getTopic(), message);
            log.debug("Published message to topic: {}", topic.getTopic());
        } catch (Exception e) {
            log.error("Failed to publish message to topic: {}", topic.getTopic(), e);
        }
    }

    public void subscribe(ChannelTopic topic, MessageListener listener) {
        try {
            listenerContainer.addMessageListener(listener, topic);
            log.info("Subscribed listener to topic: {}", topic.getTopic());
        } catch (Exception e) {
            log.error("Failed to subscribe to topic: {}", topic.getTopic(), e);
        }
    }


    public void unsubscribe(ChannelTopic topic, MessageListener listener) {
        try {
            listenerContainer.removeMessageListener(listener, topic);
            log.info("Unsubscribed listener from topic: {}", topic.getTopic());
        } catch (Exception e) {
            log.error("Failed to unsubscribe from topic: {}", topic.getTopic(), e);
        }
    }

    @Override
    public void publish(String topic, Object message) {
        try {
            redisTemplate.convertAndSend(topic, message);
            log.debug("Published message to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish message to topic: {}", topic, e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return CompletableFuture.runAsync(() -> {
            try {
                redisTemplate.convertAndSend(topic, message);
                log.debug("Async published message to topic: {}", topic);
            } catch (Exception e) {
                log.error("Failed to async publish message to topic: {}", topic, e);
            }
        });
    }
}