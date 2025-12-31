package com.photon.apigateway.configuration;

import com.photon.apigateway.cache.SelectiveCachingRouteLocator;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableScheduling
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteDefinitionLocator locator, List<RoutePredicateFactory> predicates, List<GatewayFilterFactory> gatewayFilters, GatewayProperties gatewayProperties, ConfigurationService configurationService) {
        return this.selectiveRouteLocator(locator, predicates, gatewayFilters, gatewayProperties, configurationService);
    }

    @Bean
    public SelectiveCachingRouteLocator selectiveRouteLocator(RouteDefinitionLocator locator, List<RoutePredicateFactory> predicates, List<GatewayFilterFactory> gatewayFilters, GatewayProperties gatewayProperties, ConfigurationService configurationService) {
        RouteDefinitionRouteLocator delegate = new RouteDefinitionRouteLocator(locator, predicates, gatewayFilters, gatewayProperties, configurationService);
        CompositeRouteLocator composite = new CompositeRouteLocator(Flux.just(delegate));
        return new SelectiveCachingRouteLocator(composite);
    }

    @Bean
    public InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository() {
        return new InMemoryRouteDefinitionRepository();
    }

    @Bean
    public RouteDefinitionWriter routeDefinitionWriter(InMemoryRouteDefinitionRepository repo) {
        return repo;
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            String ip;
            if (xff != null && !xff.isBlank()) {
                ip = xff.split(",")[0].trim();
            } else {
                ip = exchange.getRequest().getRemoteAddress() != null ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            }
            return Mono.just(ip);
        };
    }

}