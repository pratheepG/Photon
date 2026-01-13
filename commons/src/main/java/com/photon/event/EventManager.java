package com.photon.event;

import java.util.concurrent.CompletableFuture;

public interface EventManager {
    CompletableFuture<Void> publishAsync(String topic, Object message);
    void publish(String topic, Object message);
    <T> EventListener<T> createListener(EventListenerConfig<T> config);
}