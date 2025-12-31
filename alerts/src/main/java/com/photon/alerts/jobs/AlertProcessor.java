package com.photon.alerts.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.photon.alerts.AlertRequestConverter;
import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.enums.AlertAudience;
import com.photon.alerts.proto.AlertRequest;
import com.photon.alerts.provider.AlertDispatcher;
import com.photon.alerts.repository.SubscriberRepository;
import com.photon.alerts.service.AlertService;
import com.photon.alerts.service.SubscriberService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AlertProcessor {

    private static final String ALERT_QUEUE = "alert-queue";
    private static final String RETRY_HEADER = "retry-count";
    private static final String DLQ_TOPIC = "alert-dead-letter-queue";
    private static final int MAX_RETRIES = 3;
    private static final int MAX_QUEUE_CAPACITY = 100;
    private static final int BATCH_SIZE = 4;

    private final ReactiveZSetOperations<String, String> zSetOps;
    private final KafkaSender<String, Object> kafkaSender;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;
    private final AlertDispatcher alertDispatcher;
    private final ThreadPoolExecutor executor;
    private final SubscriberService subscriberService;
    private final SubscriberRepository subscriberRepository;

    private Disposable alertLoopSubscription;

    public AlertProcessor(ReactiveStringRedisTemplate redisTemplate, KafkaSender<String, Object> kafkaSender, ObjectMapper objectMapper, AlertService alertService, AlertDispatcher alertDispatcher, SubscriberService subscriberService, SubscriberRepository subscriberRepository) {

        this.zSetOps = redisTemplate.opsForZSet();
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.alertService = alertService;
        this.alertDispatcher = alertDispatcher;
        this.subscriberService = subscriberService;
        this.subscriberRepository = subscriberRepository;

        this.executor = new ThreadPoolExecutor(
                5, 10,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(MAX_QUEUE_CAPACITY)
        );
    }

    @PostConstruct
    public void startProcessorLoop() {
        alertLoopSubscription = Flux.interval(Duration.ofSeconds(2))
                .doOnNext(tick -> checkAndProcessAlerts())
                .doOnCancel(() -> log.info("🛑 Alert processing loop cancelled."))
                .subscribe();
    }

    @PreDestroy
    public void shutdownProcessorLoop() {
        if (alertLoopSubscription != null && !alertLoopSubscription.isDisposed()) {
            alertLoopSubscription.dispose();
            log.info("🧹 Gracefully shut down alert processor loop.");
        }
    }

    private void checkAndProcessAlerts() {
        int capacityLeft = executor.getQueue().remainingCapacity();
        Range<Long> range = Range.closed(0L, (long) (BATCH_SIZE - 1));
        if (capacityLeft < BATCH_SIZE) {
            log.warn("⛔ Queue overloaded. Skipping Redis fetch.");
            return;
        }

        zSetOps.range(ALERT_QUEUE, range)
                .collectList()
                .doOnNext(alerts -> log.info("📦 Fetched {} alert(s) from Redis", alerts.size()))
                .flatMapMany(Flux::fromIterable)
                .flatMap(alertJson ->
                        zSetOps.remove(ALERT_QUEUE, alertJson)
                                .flatMap(removed -> {
                                    if (removed != null && removed > 0L) {
                                        log.debug("🗑️ Removed alert from ZSET (locked for processing): {}", alertJson);
                                        executor.submit(() -> processAlert(alertJson));
                                    } else {
                                        log.debug("ℹ️ Could not remove alert (maybe already processing): {}", alertJson);
                                    }
                                    return Mono.empty();
                                })
                )
                .then()
                .subscribe();
    }

    private void processAlert(String alertJson) {
        int retryCount = extractRetryCount(alertJson);

        AlertRequest.Builder builder = AlertRequest.newBuilder();
        try {
            JsonFormat.parser()
                    .ignoringUnknownFields()
                    .merge(alertJson, builder);
        } catch (InvalidProtocolBufferException e) {
            log.error("❌ Failed to parse alert JSON to Protobuf", e);
            handleRetry(alertJson, retryCount).subscribe();
            return;
        }

        AlertRequest request = builder.build();
        AlertEventDto alertRequestDto = AlertRequestConverter.convertFromProtobuf(request);

        alertService.getAlertByAlertTypeAndAlertSubType(request.getAlertType(), request.getAlertSubType())
                .flatMap(alertDto -> {
                    Mono<Void> dispatchMono;

                    if (alertRequestDto.getAlertAudience() == AlertAudience.TOPIC) {
                        dispatchMono = subscriberService.getSubscribersByTopic(alertRequestDto.getTopic())
                                .flatMap(subscriber -> alertDispatcher.dispatch(alertDto, subscriber, request))
                                .then();
                    } else if (alertRequestDto.getAlertAudience() == AlertAudience.INDIVIDUAL) {
                        if (request.getIsPreLogin() && request.getIsPassword()) {
                            dispatchMono = alertDispatcher.dispatch(alertDto, request);
                        } else if (!request.getIsPreLogin() && request.getIsPassword()) {
                            dispatchMono = subscriberRepository.findByUserId(alertRequestDto.getUserId())
                                    .flatMap(subscriber -> alertDispatcher.dispatch(alertDto, subscriber, request));
                        } else if (!request.getIsPreLogin()) {
                            dispatchMono = subscriberRepository.findByUserId(alertRequestDto.getUserId())
                                    .flatMap(subscriber -> alertDispatcher.dispatch(alertDto, subscriber, request));
                        } else {
                            dispatchMono = Mono.empty();
                        }
                    } else {
                        dispatchMono = Mono.empty();
                    }

                    return dispatchMono;
                })
                .then(zSetOps.remove(ALERT_QUEUE, alertJson))
                .doOnError(e -> log.error("⚠️ Processing failed for alert: {}", alertJson, e))
                .onErrorResume(e -> handleRetry(alertJson, retryCount).thenReturn(0L))
                .subscribe();
    }

    private Mono<Long> handleRetry(String alertJson, int retryCount) {
        if (retryCount >= MAX_RETRIES) {
            log.warn("☠️ Max retries reached. Moving to DLQ: {}", alertJson);
            return sendToDLQ(alertJson).thenReturn(0L);
        }

        int nextRetryCount = retryCount + 1;
        String updatedAlert = updateRetryHeader(alertJson, nextRetryCount);
        double originalPriority = extractPriorityScore(alertJson);

        return zSetOps.add(ALERT_QUEUE, updatedAlert, originalPriority)
                .doOnNext(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        log.info("🔁 Retried alert re-added with priority {}", originalPriority);
                    } else {
                        log.warn("❌ Retry alert could not be re-added to Redis");
                    }
                })
                .map(success -> success ? 1L : 0L);
    }

    private Mono<Void> sendToDLQ(String alertJson) {

        AlertEventDto eventDto = objectMapper.convertValue(alertJson, AlertEventDto.class);
        AlertRequest request = AlertRequestConverter.convertToProtobuf(eventDto);
        return kafkaSender.send(Mono.just(SenderRecord.create(DLQ_TOPIC, null, null, null, request, null)))
                .doOnNext(result -> log.info("✅ Sent to DLQ: {}", alertJson))
                .doOnError(e -> log.error("❌ DLQ failure: {}", e.getMessage()))
                .then();
    }

    private int extractRetryCount(String alertJson) {
        try {
            JsonNode node = objectMapper.readTree(alertJson);
            return Optional.ofNullable(node.get(RETRY_HEADER)).map(JsonNode::asInt).orElse(0);
        } catch (Exception e) {
            log.error("❌ Could not parse retry count. Defaulting to 0", e);
            return 0;
        }
    }

    private double extractPriorityScore(String alertJson) {
        try {
            JsonNode node = objectMapper.readTree(alertJson);
            return Optional.ofNullable(node.get("priority")).map(JsonNode::asDouble).orElse(5.0);
        } catch (Exception e) {
            log.error("❌ Failed to extract priority. Using default.", e);
            return 5.0;
        }
    }

    private String updateRetryHeader(String alertJson, int retryCount) {
        try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(alertJson);
            node.put(RETRY_HEADER, retryCount);
            return node.toString();
        } catch (Exception e) {
            log.error("❌ Failed to update retry header", e);
            return alertJson;
        }
    }
}