package com.photon.event.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedisEventListener<T> implements EventListener<T> {

    private final RedisMessageListenerContainer container;
    private final EventListenerConfig<T> config;
    private final ObjectMapper objectMapper;
    private final List<ChannelTopic> topics = new ArrayList<>();

    private MessageListener delegate;

    public RedisEventListener(RedisMessageListenerContainer container,
                              ObjectMapper objectMapper,
                              EventListenerConfig<T> config) {
        this.container = container;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Override
    public void start() {
        delegate = this::onMessage;

        config.getTopics().forEach(topic -> {
            ChannelTopic channelTopic = new ChannelTopic(topic);
            topics.add(channelTopic);
            container.addMessageListener(delegate, channelTopic);
        });

        log.info("✅ Redis listener started for topics {}", config.getTopics());
    }

    @Override
    public void stop() {
        topics.forEach(topic ->
                container.removeMessageListener(delegate, topic)
        );
        log.info("🛑 Redis listener stopped");
    }

    private void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            T payload = objectMapper.readValue(body, config.getPayloadType());
            config.getHandler().accept(payload);
        } catch (Exception e) {
            log.error("❌ Redis message processing failed", e);
        }
    }
}