package com.photon.event.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
public class ReactivePostgresEventListener<T> implements EventListener<T> {

    private static final int MAX_RETRIES = 3;

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;
    private final EventListenerConfig<T> config;

    private Disposable subscription;

    public ReactivePostgresEventListener(DatabaseClient databaseClient,
                                         ObjectMapper objectMapper,
                                         EventListenerConfig<T> config) {
        this.databaseClient = databaseClient;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Override
    public void start() {

        subscription = Flux.interval(config.getPollInterval())
                .flatMap(tick -> pollBatch())
                .flatMap(this::processEvent)
                .onErrorContinue((e, o) ->
                        log.error("❌ Reactive PostgreSQL listener error", e))
                .subscribe();

        log.info("⚡ Reactive PostgreSQL listener started for topics {}",
                config.getTopics());
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("🛑 Reactive PostgreSQL listener stopped");
        }
    }

    // ================= CORE =================

    private Flux<EventRow> pollBatch() {
        return databaseClient.sql("""
                SELECT id, topic, payload, retry_count
                FROM event_queue
                WHERE status = 'PENDING'
                  AND topic IN (%s)
                  AND available_at <= now()
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
                """.formatted(inClause()))
                .bind("limit", config.getBatchSize())
                .map((row, meta) -> new EventRow(
                        row.get("id", Long.class),
                        row.get("topic", String.class),
                        row.get("payload", String.class),
                        row.get("retry_count", Integer.class)
                ))
                .all();
    }

    private Mono<Void> processEvent(EventRow event) {
        return Mono.fromCallable(() ->
                        objectMapper.readValue(
                                event.payload(), config.getPayloadType()))
                .doOnNext(config.getHandler())
                .then(ack(event))
                .onErrorResume(ex -> handleFailure(event, ex));
    }

    private Mono<Void> ack(EventRow event) {
        return databaseClient.sql(
                        "DELETE FROM event_queue WHERE id = :id")
                .bind("id", event.id())
                .then();
    }

    private Mono<Void> handleFailure(EventRow event, Throwable ex) {

        if (event.retryCount() >= MAX_RETRIES) {
            return databaseClient.sql("""
                    INSERT INTO event_dead_letter (topic, payload, reason)
                    VALUES (:topic, :payload::jsonb, :reason)
                    """)
                    .bind("topic", event.topic())
                    .bind("payload", event.payload())
                    .bind("reason", ex.getMessage())
                    .then()
                    .then(ack(event));
        }

        return databaseClient.sql("""
                UPDATE event_queue
                SET retry_count = retry_count + 1,
                    available_at = now() + INTERVAL '5 seconds'
                WHERE id = :id
                """)
                .bind("id", event.id())
                .then();
    }

    private String inClause() {
        return config.getTopics()
                .stream()
                .map(t -> "'" + t + "'")
                .collect(Collectors.joining(","));
    }

    record EventRow(Long id, String topic, String payload, Integer retryCount) {}
}
