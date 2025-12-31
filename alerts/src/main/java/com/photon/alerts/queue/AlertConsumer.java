package com.photon.alerts.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.photon.alerts.AlertRequestConverter;
import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.enums.Priority;
import com.photon.alerts.proto.AlertRequest;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AlertConsumer {

    private static final String ALERT_QUEUE = "alert-queue";
    private static final String RETRY_HEADER = "retry-count";
    private static final int MAX_RETRIES = 3;

    private final ReactiveZSetOperations<String, String> zSetOperations;
    private final ReceiverOptions<String, Object> receiverOptions;
    private final KafkaSender<String, Object> kafkaSender;
    private final ObjectMapper objectMapper;
    private final Disposable subscription;

    public AlertConsumer(ReactiveStringRedisTemplate redisTemplate, ReceiverOptions<String, Object> receiverOptions, KafkaSender<String, Object> kafkaSender, ObjectMapper objectMapper, Disposable subscription) {
        this.zSetOperations = redisTemplate.opsForZSet();
        this.receiverOptions = receiverOptions;
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.subscription = subscription;

        Set<String> topics = Set.of(
                Priority.HIGH.getBrokerTopic(),
                Priority.MEDIUM_HIGH.getBrokerTopic(),
                Priority.MEDIUM_LOW.getBrokerTopic(),
                Priority.LOW.getBrokerTopic()
        );

        this.listenToTopic(topics);
    }

    @PreDestroy
    public void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Kafka consumer subscription disposed");
        }
    }

    private Mono<Void> processMessage(ReceiverRecord<String, Object> record) {
        String topic = record.topic();
        double priorityScore = getPriorityScore(topic);
        int retryCount = getRetryCount(record.headers());

        return Mono.just(record.value())
                .cast(AlertRequest.class)
                .flatMap(alertEvent -> {
                    try {
                        String alertJson = JsonFormat.printer().includingDefaultValueFields().print(alertEvent);
                        return zSetOperations.add(ALERT_QUEUE, alertJson, priorityScore)
                                .flatMap(success -> {
                                    if (Boolean.TRUE.equals(success)) {
                                        log.info("✅ Alert added to queue successfully: {}", alertJson);
                                        return Mono.empty();
                                    }
                                    log.warn("⚠️ Alert not added to queue, handling retry logic...");
                                    return handleRetry(topic, record, alertJson, retryCount);
                                });
                    } catch (Exception e) {
                        log.error("JSON processing error", e);
                        return handleRetry(topic, record, record.value().toString(), retryCount);
                    }
                });
    }

    private Mono<Void> handleRetry(String topic, ReceiverRecord<String, Object> record, String alertJson, int retryCount) {
        if (retryCount >= MAX_RETRIES) {
            log.warn("⚠️ Max retry limit reached, moving to DLQ: {}", alertJson);
            return sendToDeadLetterQueue(alertJson);
        }

        int nextRetryCount = retryCount + 1;
        log.info("🔄 Retrying alert, count: {}", nextRetryCount);

        return kafkaSender.send(Mono.just(
                        SenderRecord.create(
                                topic,
                                null, null, null, alertJson,
                                List.of(new RecordHeader(RETRY_HEADER, String.valueOf(nextRetryCount).getBytes(StandardCharsets.UTF_8)))
                        )))
                .doOnNext(result -> log.debug("✅ Alert re-sent for retry: {}", alertJson))
                .doOnError(e -> log.error("❌ Failed to re-send alert for retry: {}", e.getMessage()))
                .then();
    }

    private Mono<Void> sendToDeadLetterQueue(String alertJson) {
        AlertEventDto eventDto = objectMapper.convertValue(alertJson, AlertEventDto.class);
        AlertRequest request = AlertRequestConverter.convertToProtobuf(eventDto);
        return kafkaSender.send(Mono.just(SenderRecord.create("alert-dead-letter-queue", null, null, null, request, null)))
                .doOnNext(result -> log.debug("✅ Sent to DLQ: {}", alertJson))
                .doOnError(e -> log.error("❌ Failed to send to DLQ: {}", e.getMessage()))
                .then();
    }

    private int getRetryCount(Headers headers) {
        return Optional.ofNullable(headers.lastHeader(RETRY_HEADER))
                .map(header -> new String(header.value(), StandardCharsets.UTF_8))
                .map(Integer::parseInt)
                .orElse(0);
    }

    public void listenToTopic(Set<String> topics) {
        log.info("🎧 Listening to topics: {}", topics);

        ReceiverOptions<String, Object> options = receiverOptions.subscription(topics);
        KafkaReceiver<String, Object> kafkaReceiver = KafkaReceiver.create(options);

        kafkaReceiver.receive()
                .doOnNext(record -> log.debug("📩 Received message on {}: {}", record.topic(), record.value()))
                .doOnError(e -> log.error("⚠️ Kafka consumer error", e))
                .subscribe(record -> {
                    processMessage(record).subscribe();
                    record.receiverOffset().acknowledge();
                });
    }

    private double getPriorityScore(String topic) {
        return switch (topic) {
            case "high-priority-alerts" -> 1.0;
            case "medium-high-priority-alerts" -> 2.0;
            case "medium-priority-alerts" -> 3.0;
            case "medium-low-priority-alerts" -> 4.0;
            case "low-priority-alerts" -> 5.0;
            default -> 6.0;
        };
    }
}