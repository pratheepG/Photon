package com.photon.content.location.controller;

import com.photon.content.location.service.LocalityImportService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/locality")
public class LocalityImportController {

    private final LocalityImportService importService;

    public LocalityImportController(LocalityImportService importService) {
        this.importService = importService;
    }

    @GetMapping(value = "/import", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> importLocality() {
        return importService.startImportAndStreamProgress()
                .map(sse -> {
                    assert sse.data() != null;
                    assert sse.event() != null;
                    return ServerSentEvent.builder(sse.data()).event(sse.event()).build();
                });
    }
}