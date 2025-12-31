package com.photon.content.bootstrap;

import com.photon.content.location.service.LocalityImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupImportRunner {

    private final LocalityImportService importService;

    public StartupImportRunner(LocalityImportService importService) {
        this.importService = importService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("ApplicationReadyEvent received — attempting to start locality import (if needed).");
        importService.startImportIfNeeded().subscribe(
                null,
                err -> log.error("Startup import failed", err),
                () -> log.info("Startup import flow finished (or skipped).")
        );
    }
}