package com.photon.alerts;

import com.photon.alerts.dto.AlertEventDto;
import com.photon.alerts.enums.Priority;
import com.photon.event.EventManager;
import com.photon.event.EventManagerFactory;
import com.photon.properties.ApplicationConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RefreshScope
@Conditional(AlertProducer.MessagingInfrastructurePresent.class)
public class AlertProducer {

    private final EventManager eventManager;

    public AlertProducer(ApplicationConfigProperties applicationConfigProperties,
                         ApplicationContext applicationContext) {

        EventManager manager = null;

        try {
            if (applicationConfigProperties.getEventBroker() != null) {
                manager = EventManagerFactory.getInstance(applicationConfigProperties.getEventBroker(), applicationContext);
            } else {
                log.warn("⚠️ eventBroker not configured, AlertProducer will be a no-op.");
            }
        } catch (NoClassDefFoundError e) {
            log.error("❌ Event broker [{}] configured but required library is missing ({}). AlertProducer will be disabled in this service.",
                    applicationConfigProperties.getEventBroker(),
                    e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to initialize EventManager for broker [{}]. AlertProducer will be disabled.",
                    applicationConfigProperties.getEventBroker(), e);
        }

        this.eventManager = manager;
    }

    public void sendAlert(Priority priority, AlertEventDto alertRequest) {
        if (eventManager == null) {
            log.warn("⚠️ Skipping alert send: EventManager is null (no event broker configured).");
            return;
        }

        try {
            com.photon.alerts.proto.AlertRequest protoMessage =
                    AlertRequestConverter.convertToProtobuf(alertRequest);

            this.eventManager.publishAsync(priority.getBrokerTopic(), protoMessage)
                    .exceptionally(ex -> {
                        log.error("❌ Failed to publish alert event: {}", ex.getMessage(), ex);
                        return null;
                    });

            log.info("🔥 Alert event triggered asynchronously for topic: {}", priority.getBrokerTopic());
        } catch (Exception e) {
            log.error("❌ Exception occurred while publishing alert", e);
        }
    }

    /**
     * Bean is created if ANY of the following infra is present:
     *  - Spring Kafka
     *  - Reactor Kafka
     *  - Spring RabbitMQ
     *  - Reactor RabbitMQ
     *  - Reactive Redis
     *  - Redis
     */
    static class MessagingInfrastructurePresent extends AnyNestedCondition {

        MessagingInfrastructurePresent() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnClass(name = "org.springframework.kafka.core.ProducerFactory")
        static class SpringKafkaPresent {}

        @ConditionalOnClass(name = "reactor.kafka.sender.KafkaSender")
        static class ReactorKafkaPresent {}

        @ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
        static class SpringRabbitPresent {}

        @ConditionalOnClass(name = "reactor.rabbitmq.Sender")
        static class ReactorRabbitPresent {}

        @ConditionalOnClass(name = "org.springframework.data.redis.core.ReactiveRedisTemplate")
        static class ReactiveRedisPresent {}

        @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
        static class RedisPresent {}
    }
}