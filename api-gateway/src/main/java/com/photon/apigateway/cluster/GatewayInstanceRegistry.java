package com.photon.apigateway.cluster;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayInstanceRegistry {

    private final ReactiveStringRedisTemplate redis;

    @Getter
    @Value("${spring.application.name:gateway}")
    private String appName;

    @Getter
    @Value("${gateway.instance-id:${spring.application.instance-id:${random.value}}}")
    private String instanceId; // ensure unique per pod (env var, hostname, or random)

    private String instancesKey() { return appName + ":instances"; }
    private String heartbeatKey() { return appName + ":hb:" + instanceId; }

    @PostConstruct
    public void register() {
        // Add this instance to the set and create heartbeat with TTL
        redis.opsForSet().add(instancesKey(), instanceId).then(
            redis.opsForValue().set(heartbeatKey(), "1", Duration.ofSeconds(20))
        ).subscribe(v -> log.info("Registered gateway instance {}", instanceId));
    }

    // Heartbeat every 10s; TTL 20s (so dead instances fall out naturally)
    @Scheduled(fixedDelay = 10_000)
    public void heartbeat() {
        redis.opsForValue().set(heartbeatKey(), "1", Duration.ofSeconds(20))
            .subscribe();
    }
}