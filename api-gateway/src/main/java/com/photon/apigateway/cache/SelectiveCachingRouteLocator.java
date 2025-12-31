package com.photon.apigateway.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class SelectiveCachingRouteLocator implements RouteLocator {

    private final RouteLocator delegate;
    private final AtomicReference<Map<String, Route>> cacheRef = new AtomicReference<>(Map.of());

    public SelectiveCachingRouteLocator(RouteLocator delegate) {
        this.delegate = delegate;
        delegate.getRoutes()
                .collectList()
                .map(this::toMap)
                .doOnNext(cacheRef::set)
                .subscribe();
    }

    @Override
    public Flux<Route> getRoutes() {
        return Flux.fromIterable(cacheRef.get().values());
    }

    /**
     * @return Mono<Void>
     * @method refreshAll
     * @note:- Rebuild the whole snapshot (fallback if you really need it).
     */
    public Mono<Void> refreshAll() {

        log.info("Routes : {}",delegate.getRoutes().collectList().then());

        return delegate.getRoutes()
                .collectList()
                .doOnNext(list -> cacheRef.set(toMap(list)))
                .then();
    }

    /**
     * @param routeId String
     * @return Mono<Void>
     * @method refreshOne
     * @note:- Upsert only one route by id (add/update if exists in delegate; remove if not).
     */
    public Mono<Void> refreshOne(String routeId) {
        return delegate.getRoutes()
                .filter(r -> r.getId().equals(routeId))
                .next()
                .flatMap(route -> Mono.fromRunnable(() ->
                        cacheRef.updateAndGet(old -> {
                            Map<String, Route> copy = new HashMap<>(old);
                            copy.put(routeId, route);   // update/add
                            return Collections.unmodifiableMap(copy);
                        })
                ))
                .switchIfEmpty(Mono.fromRunnable(() ->
                        cacheRef.updateAndGet(old -> {
                            Map<String, Route> copy = new HashMap<>(old);
                            copy.remove(routeId);       // evict if missing
                            return Collections.unmodifiableMap(copy);
                        })
                )).then();
    }

    /**
     * @param routeId String
     * @return Mono<Void>
     * @method refreshOneAsync
     * @note:- Upsert only one route by id (add/update if exists in delegate; remove if not) Async.
     */
    public Mono<Void> refreshOneAsync(String routeId) {
        return Mono.defer(() ->
                        delegate.getRoutes().filter(r -> r.getId().equals(routeId)).next()
                )
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .defaultIfEmpty(null)
                .doOnNext(routeOrNull -> cacheRef.updateAndGet(old -> {
                    Map<String, Route> copy = new HashMap<>(old);
                    if (routeOrNull == null) copy.remove(routeId);
                    else copy.put(routeId, routeOrNull);
                    return Collections.unmodifiableMap(copy);
                }))
                .then();
    }

    /**
     * @param routeId String
     * @return Mono<Void>
     * @method evict
     * @note:- Remove one route without touching the delegate..
     */
    public Mono<Void> evict(String routeId) {
        return Mono.fromRunnable(() ->
                cacheRef.updateAndGet(old -> {
                    if (!old.containsKey(routeId)) return old;
                    Map<String, Route> copy = new HashMap<>(old);
                    copy.remove(routeId);
                    return Collections.unmodifiableMap(copy);
                })
        );
    }

    private Map<String, Route> toMap(List<Route> routes) {
        return Collections.unmodifiableMap(
                routes.stream().collect(Collectors.toMap(Route::getId, r -> r, (a, b) -> b, LinkedHashMap::new))
        );
    }
}