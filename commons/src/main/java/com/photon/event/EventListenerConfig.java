package com.photon.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@Builder
public class EventListenerConfig<T> {
    private Set<String> topics;           // logical topics / channels
    private Class<T> payloadType;          // AlertRequest.class
    private Consumer<T> handler;           // business logic
    private Duration pollInterval;         // mainly for DB / Redis
    private int batchSize;
}