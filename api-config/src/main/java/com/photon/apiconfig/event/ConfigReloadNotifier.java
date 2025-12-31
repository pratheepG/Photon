package com.photon.apiconfig.event;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ConfigReloadNotifier {

    private final ApplicationEventPublisher eventPublisher;

    public ConfigReloadNotifier(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void triggerRefresh() {
        eventPublisher.publishEvent(new RefreshEvent(this, "manual-resolver", "Trigger config reload after placeholder resolution"));
    }
}