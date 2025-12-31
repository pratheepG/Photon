package com.photon.content.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.content.location.dto.StateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class LocalitiesJsonFluxReader {


    private final Mono<List<StateDto>> cachedListMono;

    public LocalitiesJsonFluxReader(ObjectMapper objectMapper, @Value("classpath:batch/localities.json") Resource resource) {

        this.cachedListMono = Mono.<List<StateDto>>fromCallable(() -> {
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            JsonNode records = root.has("records") ? root.get("records") : root;
            return objectMapper.readerForListOf(StateDto.class).readValue(records);
        }).cache();

    }

    public Flux<StateDto> flux() {
        return cachedListMono.flatMapMany(Flux::fromIterable);
    }

    public Mono<List<StateDto>> all() {
        return cachedListMono;
    }
}