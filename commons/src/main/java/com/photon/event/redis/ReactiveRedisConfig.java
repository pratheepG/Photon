package com.photon.event.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.data.redis.RedisReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(name = "org.springframework.data.redis.core.ReactiveRedisTemplate")
public class ReactiveRedisConfig {

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        log.info("Creating ReactiveStringRedisTemplate Bean");
        return new ReactiveStringRedisTemplate(factory);
    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port) throws UnknownHostException {
        log.info("Creating ReactiveRedisConnectionFactory Bean with host: {}, port: {}", host, port);
        InetAddress resolved = InetAddress.getByName(host);
        log.warn("Resolved host '{}' to IP: {}", host, resolved.getHostAddress());
        return new LettuceConnectionFactory(host, port);
    }

//    @Bean
//    public ReactiveHealthIndicator customRedisHealthIndicator(ReactiveRedisConnectionFactory factory) {
//        return () -> factory.getReactiveConnection()
//                .ping()
//                .map(pong -> pong.equalsIgnoreCase("PONG")
//                        ? Health.up().withDetail("message", "Redis is reaching out!").build()
//                        : Health.down().withDetail("message", "Unexpected response").build())
//                .onErrorResume(ex -> Mono.just(Health.down(ex).build()));
//    }

    @Bean
    public RedisReactiveHealthIndicator redisHealthIndicator(ReactiveRedisConnectionFactory factory) {
        return new RedisReactiveHealthIndicator(factory);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
        log.info("✅ Creating ReactiveRedisTemplate<String, Object>");

        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(new GenericJackson2JsonRedisSerializer(objectMapper))
                .hashKey(new StringRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer(objectMapper))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}