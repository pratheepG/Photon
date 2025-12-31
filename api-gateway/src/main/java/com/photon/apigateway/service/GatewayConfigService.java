//package com.photon.apigateway.service;
//
//import com.photon.apigateway.entity.GatewayLimit;
//import com.photon.apigateway.repository.GatewayLimitRepository;
//import com.photon.dto.ApiResponseDto;
//import com.photon.enums.SuccessEnum;
//import jakarta.annotation.PostConstruct;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import reactor.core.publisher.Sinks;
//
//import java.util.concurrent.atomic.AtomicLong;
//
//@Service
//public class GatewayConfigService {
//
//    public static final String KEY_GLOBAL_MAX_BYTES = "GLOBAL_MAX_BYTES";
//    public static final String KEY_GLOBAL_RATE_REPLENISH = "GLOBAL_RATE_REPLENISH";
//    public static final String KEY_GLOBAL_RATE_BURST = "GLOBAL_RATE_BURST";
//
//    private final GatewayLimitRepository repo;
//
//    private final AtomicLong globalMaxBytes = new AtomicLong(200L * 1024L * 1024L);
//    private final AtomicLong rateReplenish = new AtomicLong(10L);
//    private final AtomicLong rateBurst = new AtomicLong(20L);
//
//    private final Sinks.Many<String> changes = Sinks.many().multicast().onBackpressureBuffer();
//
//    public GatewayConfigService(GatewayLimitRepository repo) {
//        this.repo = repo;
//    }
//
//    @PostConstruct
//    public void init() {
//        repo.findByKeyName(KEY_GLOBAL_MAX_BYTES)
//                .map(GatewayLimit::getValue)
//                .defaultIfEmpty(globalMaxBytes.get())
//                .doOnNext(globalMaxBytes::set)
//                .subscribe();
//
//        repo.findByKeyName(KEY_GLOBAL_RATE_REPLENISH)
//                .map(GatewayLimit::getValue)
//                .defaultIfEmpty(rateReplenish.get())
//                .doOnNext(rateReplenish::set)
//                .subscribe();
//
//        repo.findByKeyName(KEY_GLOBAL_RATE_BURST)
//                .map(GatewayLimit::getValue)
//                .defaultIfEmpty(rateBurst.get())
//                .doOnNext(rateBurst::set)
//                .subscribe();
//    }
//
//    public long getGlobalMaxBytesValue() { return globalMaxBytes.get(); }
//    public long getRateReplenishValue() { return rateReplenish.get(); }
//    public long getRateBurstValue() { return rateBurst.get(); }
//
//    public Mono<Long> getGlobalMaxBytes() { return Mono.just(globalMaxBytes.get()); }
//    public Mono<Long> getRateReplenish() { return Mono.just(rateReplenish.get()); }
//    public Mono<Long> getRateBurst() { return Mono.just(rateBurst.get()); }
//
//    public Mono<Long> getGlobalMaxBytesValueMono() { return getGlobalMaxBytes(); }
//
//    public Sinks.Many<String> changesSink() { return changes; }
//
//    public Mono<ApiResponseDto<?>> updateLimit(String keyName, long value) {
//        return repo.findByKeyName(keyName)
//                .defaultIfEmpty(new GatewayLimit())
//                .flatMap(gl -> {
//                    if (gl.getId() == null) gl.setKeyName(keyName);
//                    gl.setValue(value);
//                    return repo.save(gl);
//                })
//                .doOnNext(saved -> {
//                    switch (keyName) {
//                        case KEY_GLOBAL_MAX_BYTES:
//                            globalMaxBytes.set(value); break;
//                        case KEY_GLOBAL_RATE_REPLENISH:
//                            rateReplenish.set(value); break;
//                        case KEY_GLOBAL_RATE_BURST:
//                            rateBurst.set(value); break;
//                        default:
//                    }
//                    changes.tryEmitNext(keyName);
//                })
//                .thenReturn(SuccessEnum.UPDATED.getSuccessResponseBody());
//    }
//}