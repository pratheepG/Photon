package com.photon.content.location.service;

import com.photon.content.location.dto.DistrictDto;
import com.photon.content.location.dto.StateDto;
import com.photon.content.location.dto.CityDto;
import com.photon.content.location.entity.District;
import com.photon.content.location.entity.State;
import com.photon.content.location.entity.City;
import com.photon.content.location.entity.LocalityImportStatus;
import com.photon.content.location.repository.CityRepository;
import com.photon.content.location.repository.DistrictRepository;
import com.photon.content.location.repository.LocalityImportStatusRepo;
import com.photon.content.location.repository.StateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class LocalityImportService {

    private static final String JOB_NAME = "locality-import";

    private final LocalitiesJsonFluxReader reader;
    private final StateRepository stateRepo;
    private final DistrictRepository districtRepo;
    private final CityRepository cityRepository;
    private final LocalityImportStatusRepo statusRepo;
    private final Sinks.Many<ImportProgress> sink;
    private final int statesPerChunk;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public LocalityImportService(LocalitiesJsonFluxReader reader,
                                 StateRepository stateRepo,
                                 DistrictRepository districtRepo,
                                 CityRepository cityRepository,
                                 LocalityImportStatusRepo statusRepo,
                                 @Value("${locality.batch.states-per-chunk:5}") int statesPerChunk) {
        this.reader = reader;
        this.stateRepo = stateRepo;
        this.districtRepo = districtRepo;
        this.cityRepository = cityRepository;
        this.statusRepo = statusRepo;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.statesPerChunk = statesPerChunk;
    }

    /**
     * Public SSE stream for clients (EventSource). This guarantees the import is started (if needed).
     */
    public Flux<ServerSentEvent<ImportProgress>> startImportAndStreamProgress() {
        startImportIfNeeded().subscribe(
                null,
                err -> log.error("import start error", err)
        );

        return sink.asFlux().map(p -> ServerSentEvent.<ImportProgress>builder()
                .event(p.type())
                .data(p)
                .build());
    }

    /**
     * Public entry to start import if not done yet. Idempotent and thread-safe.
     */
    public Mono<Void> startImportIfNeeded() {
        if (!running.compareAndSet(false, true)) {
            return Mono.fromRunnable(() -> sink.tryEmitNext(new ImportProgress("info", "Import already running", 0)));
        }

        return statusRepo.findByJobName(JOB_NAME)
                .switchIfEmpty(createInitialStatus())
                .flatMap(status -> {
                    if ("DONE".equalsIgnoreCase(status.getStatus())) {
                        sink.tryEmitNext(new ImportProgress("done", "Already completed", status.getProcessedCount() == null ? 0 : status.getProcessedCount()));
                        running.set(false);
                        return Mono.empty();
                    }
                    status.setStatus("RUNNING");
                    status.setStartedAt(Instant.now());
                    return statusRepo.save(status)
                            .then(runImportFromCheckpoint(status));
                })
                .doOnTerminate(() -> running.set(false));
    }

    private Mono<LocalityImportStatus> createInitialStatus() {
        LocalityImportStatus s = new LocalityImportStatus();
        s.setJobName(JOB_NAME);
        s.setProcessedCount(0);
        s.setStatus("PENDING");
        return statusRepo.save(s);
    }

    private Mono<Void> runImportFromCheckpoint(LocalityImportStatus checkpoint) {
        return reader.all()
                .flatMapMany(Flux::fromIterable)
                .index()
                .collectList()
                .flatMapMany(listWithIndex -> {
                    List<StateDto> all = listWithIndex.stream()
                            .map(Tuple2::getT2)
                            .toList();
                    int startIndex = 0;
                    if (checkpoint.getLastProcessedStateName() != null) {
                        for (int i = 0; i < all.size(); i++) {
                            if (all.get(i).getName().equalsIgnoreCase(checkpoint.getLastProcessedStateName())) {
                                startIndex = i + 1;
                                break;
                            }
                        }
                    }

                    return Flux.range(startIndex, Math.max(0, all.size() - startIndex))
                            .map(all::get)
                            .buffer(statesPerChunk)
                            .concatMap(this::processStateChunkSequentially);
                })
                .then(Mono.defer(() ->
                        statusRepo.findByJobName(JOB_NAME)
                                .flatMap(s -> {
                                    s.setStatus("DONE");
                                    s.setCompletedAt(Instant.now());
                                    return statusRepo.save(s)
                                            .doOnSuccess(ss -> sink.tryEmitNext(
                                                    new ImportProgress("done",
                                                            "All states processed",
                                                            ss.getProcessedCount() == null ? 0 : ss.getProcessedCount())
                                            ));
                                })
                                .then()
                ))
                .onErrorResume(err -> {
                    log.error("Import failed", err);
                    return statusRepo.findByJobName(JOB_NAME)
                            .flatMap(s -> {
                                s.setStatus("ERROR");
                                return statusRepo.save(s);
                            })
                            .doOnSuccess(s -> sink.tryEmitNext(new ImportProgress("error",
                                    err.getMessage(),
                                    s.getProcessedCount() == null ? 0 : s.getProcessedCount())))
                            .then();
                });
    }

    private Mono<Void> processStateChunkSequentially(List<StateDto> chunk) {
        return Flux.fromIterable(chunk)
                .concatMap(this::importOneStateAndUpdateCheckpoint)
                .then();
    }

    private Mono<State> importOneStateAndUpdateCheckpoint(StateDto dto) {
        String stateName = toCamelCase(dto.getName());

        return stateRepo.findByName(stateName)
                .switchIfEmpty(stateRepo.save(new State(null, stateName, dto.getCode())))
                .flatMap(savedState -> {
                    List<DistrictDto> districtDtos = dto.getDistricts();
                    if (districtDtos == null || districtDtos.isEmpty()) {
                        return Mono.just(savedState);
                    }

                    return Flux.fromIterable(districtDtos)
                            .concatMap(dDto -> {
                                String districtName = toCamelCase(dDto.getName());
                                return districtRepo.findByStateIdAndName(savedState.getId(), districtName)
                                        .switchIfEmpty(districtRepo.save(new District(null, districtName, savedState.getId(), savedState.getCode())))
                                        .flatMap(savedDistrict -> saveCitiesChunked(savedDistrict, dDto));
                            })
                            .then(Mono.just(savedState));
                })
                .flatMap(savedState ->
                        statusRepo.findByJobName(JOB_NAME)
                                .flatMap(check -> {
                                    check.setLastProcessedStateName(stateName);
                                    check.setProcessedCount((check.getProcessedCount() == null ? 0 : check.getProcessedCount()) + 1);
                                    return statusRepo.save(check)
                                            .doOnSuccess(s -> sink.tryEmitNext(new ImportProgress("processed", "Saved state " + stateName, s.getProcessedCount() == null ? 0 : s.getProcessedCount())));
                                })
                                .thenReturn(savedState)
                );
    }

    private Mono<Void> saveCitiesChunked(District savedDistrict, DistrictDto dDto) {
        List<CityDto> cities = dDto.getCities();
        if (cities == null || cities.isEmpty()) return Mono.empty();

        return Flux.fromIterable(cities)
                .map(cdto -> new City(null, cdto.getName(), cdto.getPinCode(), savedDistrict.getStateCode(), savedDistrict.getId()))
                .buffer(50)
                .concatMap(batch -> cityRepository.saveAll(batch).then())
                .then();
    }

    private String toCamelCase(String raw) {
        if (raw == null) return null;
        String[] parts = raw.toLowerCase().trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) sb.append(parts[i].substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString().trim();
    }

    public record ImportProgress(String type, String message, int processed) {}
}