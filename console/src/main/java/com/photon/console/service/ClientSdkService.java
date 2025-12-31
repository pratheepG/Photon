package com.photon.console.service;

import com.photon.console.entity.SdkArtifact;
import com.photon.console.repository.SdkArtifactRepository;
import com.photon.console.sdk.ProgressListener;
import com.photon.console.sdk.SdkGenerationProvider;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ClientSdkService {

    private final SdkGenerationProvider provider;
    private final SdkArtifactRepository sdkArtifactRepository;
    private final ExecutorService controllerExec = Executors.newCachedThreadPool();

    public ClientSdkService(SdkGenerationProvider provider, SdkArtifactRepository sdkArtifactRepository) {
        this.provider = provider;
        this.sdkArtifactRepository = sdkArtifactRepository;
    }

    public SseEmitter generateSdk(String language){

        SseEmitter emitter = new SseEmitter(0L);
        List<String> langs = Collections.singletonList(language);

        controllerExec.submit(() -> {
            try {
                ProgressListener listener = (percent, message, meta) -> {
                    try {
                        Map<String,Object> payload = new HashMap<>();
                        payload.put("percent", percent);
                        payload.put("message", message);
                        payload.put("meta", meta);
                        emitter.send(SseEmitter.event().name("progress").data(payload));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                provider.generateSingleSdkForAllModulesAndSaveWithListener(langs, "system", listener);

                emitter.send(SseEmitter.event().name("complete").data(Map.of("percent", 100, "status", "success")));
                emitter.complete();
            } catch (Throwable ex) {
                log.error("Generation failed: {}", ex.getMessage(), ex);
                try { emitter.send(SseEmitter.event().name("error").data(Map.of("message", ex.getMessage()))); } catch (Exception ignored) {}
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    public ResponseEntity<ByteArrayResource> getSdkById(UUID id){
        SdkArtifact art = this.sdkArtifactRepository.findById(id).orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));
        ByteArrayResource resource = new ByteArrayResource(art.getContent());
        String filename = art.getFilename() == null ? (id + ".zip") : art.getFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(art.getContent().length)
                .body(resource);
    }
}