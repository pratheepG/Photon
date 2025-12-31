package com.photon.apigateway.cluster;

import com.photon.apigateway.cache.JWKSourceCacheProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWKSKeyRotation {

    private final ReactiveStringRedisTemplate redis;
    private final JWKSourceCacheProvider jwkSourceCacheProvider;
    private static final String KEY_ROTATION_CMD = "gateway:refresh:jwks";

    @PostConstruct
    public void subscribe() {
        redis.listenToChannel(KEY_ROTATION_CMD)
                .map(ReactiveSubscription.Message::getMessage)
                .flatMap(this::handleMessage)
                .onErrorContinue((e, o) -> log.warn("Refresh listener error: {}", e.toString()))
                .subscribe();
        log.info("Subscribed to {}", KEY_ROTATION_CMD);
    }

    private Mono<Void> handleMessage(String msg) {
        try {
            if (msg.equalsIgnoreCase("REFRESH_JWKS")) {
                log.info("Received JWKS refresh command. Clearing local cache.");
                jwkSourceCacheProvider.refreshJwkSource();
            }
        } catch (Exception e) {
            log.warn("Bad message on {}: {}", KEY_ROTATION_CMD, msg, e);
        }
        return Mono.empty();
    }

}