package com.photon.event.rabbitmq;

import com.photon.event.EventManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.concurrent.CompletableFuture;

final public class RabbitMQEvent implements EventManager {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEvent(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String topic, Object message) {
        rabbitTemplate.convertAndSend(topic, message);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return CompletableFuture.runAsync(() -> rabbitTemplate.convertAndSend(topic, message));
    }
}