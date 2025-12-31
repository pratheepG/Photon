package com.photon.alerts;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

@Slf4j
@Configuration
public class MessagingPresenceLogger {

    private final ClassLoader classLoader;

    public MessagingPresenceLogger(ApplicationContext applicationContext) {
        this.classLoader = applicationContext.getClassLoader();
    }

    @PostConstruct
    public void logMessagingInfrastructurePresence() {
        log.info("=== Messaging infrastructure presence check ===");
        log.info("Spring Kafka ProducerFactory present      : {}",
                isPresent("org.springframework.kafka.core.ProducerFactory"));
        log.info("Reactor Kafka KafkaSender present         : {}",
                isPresent("reactor.kafka.sender.KafkaSender"));
        log.info("Spring Rabbit RabbitTemplate present      : {}",
                isPresent("org.springframework.amqp.rabbit.core.RabbitTemplate"));
        log.info("Reactor Rabbit Sender present             : {}",
                isPresent("reactor.rabbitmq.Sender"));
        log.info("Spring Data RedisTemplate present         : {}",
                isPresent("org.springframework.data.redis.core.RedisTemplate"));
        log.info("Spring Data ReactiveRedisTemplate present : {}",
                isPresent("org.springframework.data.redis.core.ReactiveRedisTemplate"));
        log.info("===============================================");
    }

    private boolean isPresent(String className) {
        return ClassUtils.isPresent(className, this.classLoader);
    }
}
