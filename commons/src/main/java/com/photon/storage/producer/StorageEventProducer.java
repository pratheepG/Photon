package com.photon.storage.producer;

import com.photon.event.EventManager;
import com.photon.event.EventManagerFactory;
import com.photon.properties.ApplicationConfigProperties;
import com.photon.storage.dto.CdnEventDto;
import com.photon.storage.util.CdnEventConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RefreshScope
public class StorageEventProducer {
    private static final String STORAGE_TOPIC = "PHOTON_STORAGE_EVENTS";
    private final EventManager eventManager;

    public StorageEventProducer(ApplicationConfigProperties applicationConfigProperties, ApplicationContext applicationContext) {
        EventManager manager = null;
        try {
            if (applicationConfigProperties.getEventBroker() != null)
                manager = EventManagerFactory.getInstance(applicationConfigProperties.getEventBroker(), applicationContext);
            else
                log.warn("⚠️ eventBroker not configured, StorageEventProducer will be a no-op.");
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

    @Async
    public void dispatch(CdnEventDto cdnEventDto) {
        try {
            com.photon.storage.proto.CdnEvent cdnEventProto = CdnEventConverter.toProtoBuf(cdnEventDto);
            this.eventManager.publishAsync(STORAGE_TOPIC, cdnEventProto)
                    .exceptionally(ex -> {
                        log.error("❌ Failed to publish storage event to Kafka: {}", ex.getMessage(), ex);
                        return null;
                    });
            log.info("🔥 Storage event triggered asynchronously for topic: {}", STORAGE_TOPIC);
        } catch (Exception e) {
            log.error("❌ Exception occurred while publishing event", e);
        }
    }
}