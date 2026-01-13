package com.photon.event.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.event.EventListener;
import com.photon.event.EventListenerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class PostgresEventListener<T> implements EventListener<T> {

    private static final int MAX_RETRIES = 3;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final EventListenerConfig<T> config;

    private ScheduledExecutorService scheduler;

    public PostgresEventListener(JdbcTemplate jdbcTemplate,
                                 ObjectMapper objectMapper,
                                 EventListenerConfig<T> config) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    // 🔑 Equivalent of Redis listenTo(...)
    @Override
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(
                this::pollAndProcess,
                0,
                config.getPollInterval().toMillis(),
                TimeUnit.MILLISECONDS
        );

        log.info("🐘 PostgreSQL listener started for topics {}", config.getTopics());
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            log.info("🛑 PostgreSQL listener stopped");
        }
    }

    // ================= CORE =================

    private void pollAndProcess() {
        try {
            List<EventRow> events = jdbcTemplate.query(
                    """
                    SELECT id, topic, payload, retry_count
                    FROM event_queue
                    WHERE status = 'PENDING'
                      AND topic IN (%s)
                      AND available_at <= now()
                    ORDER BY created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?
                    """.formatted(inClause()),
                    ps -> ps.setInt(1, config.getBatchSize()),
                    (rs, i) -> new EventRow(
                            rs.getLong("id"),
                            rs.getString("topic"),
                            rs.getString("payload"),
                            rs.getInt("retry_count")
                    )
            );

            for (EventRow event : events) {
                process(event);
            }

        } catch (Exception e) {
            log.error("❌ PostgreSQL polling error", e);
        }
    }

    private void process(EventRow event) {
        try {
            T payload = objectMapper.readValue(event.payload(), config.getPayloadType());
            config.getHandler().accept(payload);

            // ✅ ACK (delete = commit)
            jdbcTemplate.update("DELETE FROM event_queue WHERE id = ?", event.id());

        } catch (Exception ex) {
            handleFailure(event, ex);
        }
    }

    private void handleFailure(EventRow event, Exception ex) {

        if (event.retryCount() >= MAX_RETRIES) {
            // DLQ
            jdbcTemplate.update(
                    """
                    INSERT INTO event_dead_letter (topic, payload, reason)
                    VALUES (?, ?::jsonb, ?)
                    """,
                    event.topic(), event.payload(), ex.getMessage()
            );

            jdbcTemplate.update("DELETE FROM event_queue WHERE id = ?", event.id());
            return;
        }

        // Retry with backoff
        jdbcTemplate.update(
                """
                UPDATE event_queue
                SET retry_count = retry_count + 1,
                    available_at = now() + INTERVAL '5 seconds'
                WHERE id = ?
                """,
                event.id()
        );
    }

    private String inClause() {
        return config.getTopics()
                .stream()
                .map(t -> "'" + t + "'")
                .collect(Collectors.joining(","));
    }

    record EventRow(long id, String topic, String payload, int retryCount) {}
}