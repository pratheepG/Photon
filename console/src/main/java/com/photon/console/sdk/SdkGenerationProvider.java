package com.photon.console.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.photon.console.entity.EndpointDetails;
import com.photon.console.entity.SdkArtifact;
import com.photon.console.repository.EndpointDetailsRepository;
import com.photon.console.repository.SdkArtifactRepository;
import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service responsible for producing SDK artifacts (per-module or combined).
 * Simplified, synchronous save (no @Transactional here) — artifactRepo.save(...) is used directly.
 */
@Slf4j
@Service
public class SdkGenerationProvider {

    private final EndpointDetailsRepository endpointDetailsRepository;
    private final SdkArtifactRepository artifactRepo;
    private final SdkGenerator generator;
    private final Path workspaceBase;
    private final ExecutorService executor;

    private static final ObjectMapper CANONICAL_MAPPER = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public SdkGenerationProvider(EndpointDetailsRepository endpointDetailsRepository,
                                 SdkArtifactRepository artifactRepo,
                                 SdkGenerator generator,
                                 @Value("${sdkgen.workspace:/tmp/console-sdk-work}") String workspace,
                                 @Value("${sdkgen.maxThreads:2}") int maxThreads) throws Exception {

        this.endpointDetailsRepository = endpointDetailsRepository;
        this.artifactRepo = artifactRepo;
        this.generator = generator;
        this.workspaceBase = Path.of(workspace);
        Files.createDirectories(workspaceBase);
        this.executor = Executors.newFixedThreadPool(Math.max(1, maxThreads));
    }

    /**
     * Generate a single combined SDK for ALL modules (paged) and save per-module per-language artifacts.
     *
     * Progress reporting:
     *  - listener.onProgress(percent, message, meta)
     *
     * This method:
     *  - pages module ids
     *  - for each module builds deterministic JSON (loadModuleMerged)
     *  - calls generator.generateModuleSdks(...) for that module and requested languages
     *  - saves produced zip bytes into SdkArtifact rows (artifactRepo.save)
     *  - unzips into a combined workspace and finally zips the combined SDK and returns its Path
     *
     * NOTE: you said you want to save inside this method — so artifactRepo.save(...) is used directly.
     */
    public Path generateSingleSdkForAllModulesAndSaveWithListener(List<String> languages, String requestedBy, ProgressListener listener) throws Exception {

        if (languages == null || languages.isEmpty()) throw new IllegalArgumentException("languages list is required");
        if (listener == null) listener = (percent, msg, meta) -> {}; // no-op

        // final workspace (combined SDK)
        Path finalSdkRoot = workspaceBase.resolve("final-sdk-" + System.currentTimeMillis());
        Files.createDirectories(finalSdkRoot);

        // paging configuration
        final int pageSize = 100; // tune if necessary
        int page = 0;

        long totalModules = endpointDetailsRepository.count();
        if (totalModules == 0) {
            listener.onProgress(100, "No modules found", Map.of());
            Path emptyZip = workspaceBase.resolve("all-modules-sdk-" + System.currentTimeMillis() + ".zip");
            zipDirectory(finalSdkRoot, emptyZip);
            listener.onProgress(100, "Completed (no modules)", Map.of("zip", emptyZip.toString()));
            return emptyZip;
        }

        long processedModules = 0;
        List<Path> tempZipsToCleanup = new ArrayList<>();
        List<Path> tempDirsToCleanup = new ArrayList<>();

        try {
            Page<String> pageResult;
            do {
                Pageable pageable = PageRequest.of(page, pageSize);
                pageResult = endpointDetailsRepository.findAllIds(pageable);

                for (String moduleId : pageResult.getContent()) {
                    // cancellation support
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Generation cancelled");

                    processedModules++;
                    double moduleStartFraction = (double) (processedModules - 1) / (double) totalModules;
                    double moduleEndFraction = (double) processedModules / (double) totalModules;
                    int percentStart = (int) Math.round(moduleStartFraction * 85.0); // 0..85 reserved for modules
                    int percentEnd = (int) Math.round(moduleEndFraction * 85.0);

                    listener.onProgress(percentStart, "Starting module", Map.of("moduleId", moduleId, "index", processedModules, "total", totalModules));

                    // load a deterministic merged DTO (models + features/actions) to avoid JPA multi-collection fetch problems
                    Map<String, Object> merged;
                    try {
                        merged = loadModuleMerged(moduleId);
                    } catch (Exception ex) {
                        log.warn("Failed to load module {} merged DTO: {}", moduleId, ex.getMessage());
                        listener.onProgress(percentEnd, "Failed to load module metadata", Map.of("moduleId", moduleId, "error", ex.getMessage()));
                        continue;
                    }

                    String metadataJson = canonicalJson(merged);
                    String metadataHash = sha256Hex(metadataJson);

                    listener.onProgress(percentStart + 1, "Invoking generator", Map.of("moduleId", moduleId, "langs", languages.size()));

                    EndpointDetails module = endpointDetailsRepository.findByIdWithAssociations(moduleId)
                            .orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1006.getErrorResponseBody(), HttpStatus.NOT_FOUND));

                    Map<String, Map<String, Path>> generated;
                    try {
                        generated = generator.generateModuleSdksFromEntities(List.of(module), languages, workspaceBase);
                    } catch (Throwable t) {
                        log.error("Generator failed for module {}: {}", moduleId, t.getMessage(), t);
                        listener.onProgress(percentEnd, "Generator failed: " + t.getMessage(), Map.of("moduleId", moduleId));
                        continue;
                    }

                    Map<String, Path> artifactsForModule = generated == null ? null : generated.get(moduleId);
                    if (artifactsForModule == null || artifactsForModule.isEmpty()) {
                        listener.onProgress(percentEnd, "No artifacts produced for module", Map.of("moduleId", moduleId));
                        continue;
                    }

                    // create module folder inside combined SDK
                    Path moduleTargetRoot = finalSdkRoot.resolve(moduleId);
                    Files.createDirectories(moduleTargetRoot);

                    int langIndex = 0;
                    for (String lang : languages) {
                        langIndex++;
                        int langStart = percentStart + (int) Math.round((langIndex - 1) * ((percentEnd - percentStart) / (double) Math.max(1, languages.size())));
                        int langEnd = percentStart + (int) Math.round((langIndex) * ((percentEnd - percentStart) / (double) Math.max(1, languages.size())));
                        listener.onProgress(langStart, "Processing language", Map.of("moduleId", moduleId, "lang", lang));

                        Path zip = artifactsForModule.get(lang);
                        if (zip == null || !Files.exists(zip)) {
                            listener.onProgress(langEnd, "Zip missing for language", Map.of("moduleId", moduleId, "lang", lang));
                            continue;
                        }

                        // read zip bytes and persist artifact (dedupe by metadataHash + moduleId + lang)
                        byte[] bytes = Files.readAllBytes(zip);

                        // double-check if artifact already exists (dedupe)
                        boolean alreadyExists = artifactRepo.findByModuleIdAndLanguageAndMetadataHash(moduleId, lang, metadataHash).isPresent();
                        if (!alreadyExists) {
                            try {
                                SdkArtifact art = new SdkArtifact();
                                art.setModuleId(moduleId);
                                art.setLanguage(lang);
                                art.setMetadataHash(metadataHash);
                                art.setFilename(zip.getFileName().toString());
                                art.setSizeBytes(bytes.length);
                                art.setContent(bytes);
                                art.setCreatedBy(requestedBy);
                                artifactRepo.save(art);
                                listener.onProgress(Math.min(langEnd, 99), "Saved artifact", Map.of("moduleId", moduleId, "lang", lang, "artifactId", art.getId()));
                            } catch (Throwable saveEx) {
                                log.warn("Failed to save artifact for module={}, lang={}: {}", moduleId, lang, saveEx.getMessage());
                                listener.onProgress(langEnd, "Failed to persist artifact", Map.of("moduleId", moduleId, "lang", lang, "error", saveEx.getMessage()));
                            }
                        } else {
                            listener.onProgress(Math.min(langEnd, 99), "Artifact already exists", Map.of("moduleId", moduleId, "lang", lang));
                        }

                        // unzip produced zip into combined finalSdkRoot/moduleId/<lang>/
                        Path targetDir = moduleTargetRoot.resolve(lang);
                        Files.createDirectories(targetDir);
                        try {
                            unzipToDirectory(zip, targetDir);
                            // schedule temp cleanup
                            tempZipsToCleanup.add(zip);
                            Path genTemp = deriveOutDirFromZip(zip);
                            if (genTemp != null) tempDirsToCleanup.add(genTemp);
                            listener.onProgress(Math.max(langStart + 1, langEnd - 1), "Unzipped artifact", Map.of("moduleId", moduleId, "lang", lang));
                        } catch (Throwable t) {
                            log.warn("Failed to unzip {} for module {} lang {}: {}", zip, moduleId, lang, t.getMessage());
                            listener.onProgress(langEnd, "Failed to unzip artifact", Map.of("moduleId", moduleId, "lang", lang, "error", t.getMessage()));
                        }

                        // optional small module metadata file next to the language folder
                        try {
                            Path metaFile = moduleTargetRoot.resolve(lang).resolve("module.json");
                            Files.writeString(metaFile, canonicalJson(Map.of("moduleId", moduleId, "generatedAt", System.currentTimeMillis())), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        } catch (Exception e) {
                            log.debug("Failed to write module metadata: {}", e.getMessage());
                        }

                        listener.onProgress(langEnd, "Language completed", Map.of("moduleId", moduleId, "lang", lang));
                    } // end languages

                    listener.onProgress(percentEnd, "Module generation done", Map.of("moduleId", moduleId));
                } // end for each module id in page

                page++; // advance to next page
            } while (pageResult != null && pageResult.hasNext());

        } finally {
            // best-effort cleanup of generator temp outputs
            for (Path zip : tempZipsToCleanup) {
                try { if (zip != null && Files.exists(zip)) Files.deleteIfExists(zip); } catch (Exception ex) { log.debug("cleanup zip failed: {}", ex.getMessage()); }
            }
            for (Path dir : tempDirsToCleanup) {
                try { if (dir != null && Files.exists(dir)) deleteRecursively(dir); } catch (Exception ex) { log.debug("cleanup dir failed: {}", ex.getMessage()); }
            }
        }

        // finalize combined SDK (85..95% range reserved for this)
        listener.onProgress(86, "Zipping combined SDK", Map.of("target", finalSdkRoot.toString()));
        Path finalZip = workspaceBase.resolve("all-modules-sdk-" + System.currentTimeMillis() + ".zip");
        zipDirectory(finalSdkRoot, finalZip);
        listener.onProgress(95, "Final zip created", Map.of()); // not returning path in meta if you prefer

        // keep finalZip for inspection or return it
        listener.onProgress(100, "All modules SDK generation completed", Map.of());

        return finalZip;
    }

    /* -------------------------
       Helper / utility methods
       ------------------------- */

    @SuppressWarnings("unchecked")
    public Map<String,Object> loadModuleMerged(String moduleId) {
        // Implementation assumes repositories expose methods to fetch models and features separately.
        // You already had this logic previously — keep as-is (deterministic TreeMap ordering).
        EndpointDetails modelsLoaded = endpointDetailsRepository.findWithModels(moduleId)
                .orElseThrow(() -> new NoSuchElementException("module not found: " + moduleId));
        EndpointDetails featuresLoaded = endpointDetailsRepository.findWithFeaturesAndActions(moduleId)
                .orElseGet(() -> modelsLoaded);

        Map<String,Object> merged = new TreeMap<>();
        merged.put("id", modelsLoaded.getId());
        merged.put("name", modelsLoaded.getName());
        merged.put("clientId", modelsLoaded.getClientId());
        merged.put("clientSecret", modelsLoaded.getClientSecret());

        // models -> convert to plain list
        List<Map<String,Object>> models = new ArrayList<>();
        if (modelsLoaded.getModels() != null) {
            modelsLoaded.getModels().stream()
                    .sorted(Comparator.comparing(m -> m.getModelId() == null ? m.getId() : m.getModelId()))
                    .forEach(m -> {
                        Map<String,Object> mm = new TreeMap<>();
                        mm.put("id", m.getId());
                        mm.put("modelId", m.getModelId());
                        mm.put("name", m.getName());
                        mm.put("fields", new ArrayList<>(m.getFields())); // ensure plain list
                        models.add(mm);
                    });
        }
        merged.put("models", models);

        // features -> actions (from featuresLoaded)
        List<Map<String,Object>> features = new ArrayList<>();
        if (featuresLoaded.getFeatures() != null) {
            featuresLoaded.getFeatures().stream()
                    .sorted(Comparator.comparing(f -> f.getFeatureId() == null ? f.getId().toString() : f.getFeatureId()))
                    .forEach(f -> {
                        Map<String,Object> fm = new TreeMap<>();
                        fm.put("id", f.getId().toString());
                        fm.put("featureId", f.getFeatureId());
                        fm.put("path", f.getPath());
                        fm.put("name", f.getName());
                        fm.put("description", f.getDescription());

                        List<Map<String,Object>> actions = new ArrayList<>();
                        if (f.getActions() != null) {
                            f.getActions().stream()
                                    .sorted(Comparator.comparing(a -> a.getActionId() == null ? a.getId().toString() : a.getActionId()))
                                    .forEach(a -> {
                                        Map<String,Object> am = new TreeMap<>();
                                        am.put("id", a.getId().toString());
                                        am.put("actionId", a.getActionId());
                                        am.put("path", a.getPath());
                                        am.put("name", a.getName());
                                        am.put("description", a.getDescription());
                                        am.put("requestMethod", a.getRequestMethod() == null ? null : a.getRequestMethod().name());
                                        am.put("requestBodyModelId", a.getRequestBodyModelId());
                                        am.put("responseBodyModelId", a.getResponseBodyModelId());
                                        am.put("isRequestBodyCollection", a.getIsRequestBodyCollection());
                                        am.put("isResponseBodyCollection", a.getIsResponseBodyCollection());
                                        am.put("securityLevel", a.getSecurityLevel() == null ? null : a.getSecurityLevel().name());
                                        am.put("accessLevel", a.getAccessLevel() == null ? null : a.getAccessLevel().name());
                                        actions.add(am);
                                    });
                        }
                        fm.put("actions", actions);
                        features.add(fm);
                    });
        }
        merged.put("features", features);

        return merged;
    }

    private void unzipToDirectory(Path zipPath, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = targetDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(targetDir)) throw new IOException("Zip entry outside target dir: " + entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream os = Files.newOutputStream(outPath)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) os.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        if (zipFile.getParent() != null) Files.createDirectories(zipFile.getParent());
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walk(sourceDir)
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\","/"));
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private Path deriveOutDirFromZip(Path zip) {
        if (zip == null) return null;
        Path parent = zip.getParent();
        String fileName = zip.getFileName() == null ? null : zip.getFileName().toString();
        if (fileName != null && fileName.endsWith(".zip")) {
            String dirName = fileName.substring(0, fileName.length() - 4);
            return parent == null ? null : parent.resolve(dirName);
        }
        return parent;
    }

    private void deleteRecursively(Path p) throws IOException {
        if (!Files.exists(p)) return;
        Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String canonicalJson(Object obj) throws JsonProcessingException {
        return CANONICAL_MAPPER.writeValueAsString(obj);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}