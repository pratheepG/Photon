package com.photon.storage.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.storage.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class FileEntityLinkListener {

    private final FileMetadataRepository fileMetadataRepository;
    private final ReceiverOptions<String, String> receiverOptions;
    private final ObjectMapper objectMapper;

    public FileEntityLinkListener(FileMetadataRepository repo, ReceiverOptions<String, String> receiverOptions, ObjectMapper objectMapper) {
        this.fileMetadataRepository = repo;
        this.receiverOptions = receiverOptions;
        this.objectMapper = objectMapper;

        Set<String> topics = Set.of("file-entity-link");
        this.listenToTopic(topics);
    }

    public void listenToTopic(Set<String> topics) {
        log.info("🎧 Listening to topics: {}", topics);

        ReceiverOptions<String, String> options = receiverOptions.subscription(topics);
        KafkaReceiver<String, String> kafkaReceiver = KafkaReceiver.create(options);

        kafkaReceiver.receive()
                .doOnNext(record -> log.debug("📩 Received message on {}: {}", record.topic(), record.value()))
                .doOnError(e -> log.error("⚠️ Kafka consumer error", e))
                .concatMap(this::handleFileEntityLinkEvent)
                .subscribe();
    }

    private Mono<Void> handleFileEntityLinkEvent(ReceiverRecord<String, String> record) {
        return parseEvent(record.value())
                .flatMap(dto ->
                        fileMetadataRepository.findById(dto.fileMetadataId())
                                .flatMap(metadata -> {
                                    metadata.setAssociatedEntityId(dto.entityId());
                                    return fileMetadataRepository.save(metadata);
                                })
                )
                .doOnError(e -> log.error("Failed to process FileEntityLinkEvent: {}", e.getMessage()))
                .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge()));
    }

    private Mono<FileEntityLinkEvent> parseEvent(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, FileEntityLinkEvent.class));
    }

    public static record FileEntityLinkEvent(UUID fileMetadataId, String entityId) {}
}