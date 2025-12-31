package com.photon.event;

import com.photon.event.kafka.KafkaEvent;
import com.photon.event.kafka.KafkaEventReactive;
import com.photon.event.rabbitmq.RabbitMQEvent;
import com.photon.event.redis.RedisEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import reactor.kafka.sender.KafkaSender;

@Slf4j
final public class EventManagerFactory {
    private static volatile EventManager instance;

    private EventManagerFactory() {}

    public static EventManager getInstance(String broker, ApplicationContext applicationContext) {
        if (instance == null) {
            synchronized (EventManagerFactory.class) {
                if (instance == null) {
                    instance = createEventManager(broker, applicationContext);
                }
            }
        }
        return instance;
    }

    private static EventManager createEventManager(String broker, ApplicationContext applicationContext) {
        log.info("Creating EventManager for broker: {}", broker);

        boolean isReactive = isReactiveContext(applicationContext);

        return switch (broker.toLowerCase()) {
            case "kafka" -> isReactive
                    ? new KafkaEventReactive(applicationContext.getBean(KafkaSender.class))
                    : new KafkaEvent(applicationContext.getBean(org.springframework.kafka.core.KafkaTemplate.class));
            case "rabbitmq" -> new RabbitMQEvent(new RabbitTemplate());
            default -> applicationContext.getBean(RedisEvent.class);
        };
    }

    private static boolean isReactiveContext(ApplicationContext context) {
        try {
            Class.forName("org.springframework.web.reactive.config.WebFluxConfigurer");
            return context.getBeanNamesForType(Class.forName("org.springframework.web.reactive.HandlerMapping")).length > 0;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}