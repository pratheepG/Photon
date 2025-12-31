package com.photon.apigateway.cluster;

import com.photon.apigateway.service.GatewayRouteService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaggeredRouteRefresher {

    private final ReactiveStringRedisTemplate redis;
    private final GatewayInstanceRegistry registry;
    private final GatewayRouteService routeService;

    private static final String CHANNEL_CMD = "gateway:refresh:cmd";

    private String instancesKey() { return registry.getAppName() + ":instances"; }
    private String hbPrefix()     { return registry.getAppName() + ":hb:"; }

    private String queueKey(String ts) { return registry.getAppName() + ":refreshq:" + ts; }

    @PostConstruct
    public void subscribe() {
        redis.listenToChannel(CHANNEL_CMD)
                .map(ReactiveSubscription.Message::getMessage)
                .flatMap(this::handleMessage)
                .onErrorContinue((e, o) -> log.warn("Refresh listener error: {}", e.toString()))
                .subscribe();
        log.info("Subscribed to {}", CHANNEL_CMD);
    }

    /* Admin (or another service) can call this to kick off refresh */
    public Mono<Void> triggerClusterRefresh() {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        return redis.convertAndSend(CHANNEL_CMD, "REFRESH:START:" + ts)
                .then();
    }

    /* Admin (or another service) can call this to kick off refresh */
    public Mono<Void> triggerClusterRefreshForRoute(String routeId) {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        return redis.convertAndSend(CHANNEL_CMD, "REFRESH:ONE:" + routeId + ":" + ts)
                .then();
    }


    private Mono<Void> handleMessage(String msg) {
        try {
            if (msg.startsWith("REFRESH:ONE:ADDED")) {
                String[] parts = msg.split(":");
                String routeId = parts[3];
                String ts      = parts[4];
                return tryRefreshAddedRoute(routeId, ts);
            }
            if (msg.startsWith("REFRESH:ONE:REMOVED")) {
                String[] parts = msg.split(":");
                String routeId = parts[3];
                String ts      = parts[4];
                return tryRefreshRemovedRoute(routeId, ts);
            }
            if (msg.startsWith("REFRESH:START:")) {
                String ts = msg.substring("REFRESH:START:".length());
                return buildQueueIfMissing(ts)
                        .then(redis.convertAndSend(CHANNEL_CMD, "REFRESH:NEXT:" + ts).then())
                        .doOnSuccess(v -> log.info("[{}] Queue built, asked NEXT", ts));
            }
            if (msg.startsWith("REFRESH:NEXT:")) {
                String ts = msg.substring("REFRESH:NEXT:".length());
                return tryRefreshIfHead(ts);
            }
            if (msg.startsWith("REFRESH:DONE:")) {
                String ts = msg.substring("REFRESH:DONE:".length());
                log.info("[{}] Cluster refresh done", ts);
                return Mono.empty();
            }
        } catch (Exception e) {
            log.warn("Bad message on {}: {}", CHANNEL_CMD, msg, e);
        }
        return Mono.empty();
    }

    private Mono<Void> buildQueueIfMissing(String ts) {
        String qKey = queueKey(ts);

        return redis.opsForList().size(qKey)
                .flatMap(size -> {
                    if (size != null && size > 0) {
                        return Mono.empty();
                    }

                    return redis.opsForSet().scan(instancesKey())
                            .flatMap(id -> redis.hasKey(hbPrefix() + id)
                                    .filter(Boolean::booleanValue)
                                    .map(ok -> id)
                            )
                            .collectList()
                            .flatMapMany(aliveInstances -> {
                                List<String> shuffled = new ArrayList<>(aliveInstances);
                                Collections.shuffle(shuffled);
                                return redis.opsForList()
                                        .rightPushAll(qKey, shuffled.toArray(new String[0]))
                                        .thenMany(Mono.defer(() ->
                                                redis.expire(qKey, Duration.ofMinutes(5))
                                        ));
                            })
                            .then();
                });
    }

    private Mono<Void> tryRefreshIfHead(String ts) {
        String qKey = queueKey(ts);
        String me   = registry.getInstanceId();

        return redis.opsForList().index(qKey, 0)
                .flatMap(head -> {
                    if (head == null) {
                        return redis.convertAndSend(CHANNEL_CMD, "REFRESH:DONE:" + ts).then();
                    }
                    if (!me.equals(head)) {
                        return Mono.empty();
                    }

                    log.info("[{}] My turn to refresh routes", ts);

                    return routeService.refreshAllRoute()
                            .then()
                            .then(redis.opsForList().leftPop(qKey).then(redis.convertAndSend(CHANNEL_CMD, "REFRESH:NEXT:" + ts).then()))
                            .doOnError(e -> log.error("[{}] Refresh failed on {}: {}", ts, me, e.toString()));
                })
                .switchIfEmpty(redis.convertAndSend(CHANNEL_CMD, "REFRESH:DONE:" + ts).then());
    }

    private Mono<Void> tryRefreshAddedRoute(String routeId, String ts) {
        String me = registry.getInstanceId();
        log.info("[{}] Refreshing added route {}", ts, routeId);

        return routeService.refreshAddedRoute(routeId)
                .doOnSuccess(v -> log.info("[{}] Refreshed added route {} on {}", ts, routeId, me))
                .doOnError(e -> log.error("[{}] Failed refreshing added route {} on {}: {}", ts, routeId, me, e.getMessage()));
    }

    private Mono<Void> tryRefreshRemovedRoute(String routeId, String ts) {
        String me = registry.getInstanceId();
        log.info("[{}] Refreshing removed route {}", ts, routeId);

        return routeService.refreshRemovedRoute(routeId)
                .doOnSuccess(v -> log.info("[{}] Refreshed removed route {} on {}", ts, routeId, me))
                .doOnError(e -> log.error("[{}] Failed refreshing removed route {} on {}: {}", ts, routeId, me, e.getMessage()));
    }

}