package com.photon.storage.service.impl;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.storage.dto.FileResponseDto;
import com.photon.storage.entity.FileMetadata;
import com.photon.storage.enums.ImagePreset;
import com.photon.storage.repository.FileMetadataRepository;
import com.photon.storage.service.api.MediaService;
import com.photon.storage.utils.ImageResizer;
import com.photon.utils.HttpRequestManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ConsoleMediaService implements MediaService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${media.upload.folder:/tmp/uploads}")
    private String uploadRootFolder;

    public ConsoleMediaService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadImages(Flux<FilePart> files, List<String> presetNames, ServerHttpRequest serverHttpRequest) {
        List<ImagePreset> presets = convertToPresets(presetNames);
        if (presets == null || presets.isEmpty()) {
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1040.getErrorResponseBody(), HttpStatus.BAD_REQUEST));
        }

        return files.flatMap(file -> {
                    String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
                    if (contentType.startsWith("image/")) {
                        // For each file part, process all presets
                        return Flux.fromIterable(presets)
                                .flatMap(preset -> processAndSaveImage(file, preset));
                    }
                    log.warn("Skipping non-image file: {}", file.filename());
                    return Flux.empty();
                })
                .collectList()
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadVideos(Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return files.flatMap(this::processAndSaveVideo)
                .collectList()
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadFiles(Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return files.flatMap(this::processAndSaveFile)
                .collectList()
                .map(SuccessEnum.SUCCESS::getSuccessResponseBody);
    }

    private List<ImagePreset> convertToPresets(List<String> presetNames) {
        if (presetNames == null) {
            return null;
        }
        try {
            return presetNames.stream().map(String::toUpperCase).map(ImagePreset::valueOf).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid image preset name provided", e);
            return null;
        }
    }

    private Mono<FileResponseDto> processAndSaveImage(FilePart file, ImagePreset preset) {
        String originalName = file.filename();
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = preset.name().toLowerCase() + "-" + originalName;
        String relativePath = "images/" + storedName;
        Path filePath = Path.of(uploadRootFolder, relativePath);

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .collectList()
                .map(this::joinByteBuffers)
                .publishOn(Schedulers.boundedElastic())
                .map(imageBytes -> {
                    try {
                        return ImageResizer.resizeAndCompressImage(imageBytes, preset.getWidth(), preset.getHeight(), preset.getQuality());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(resizedImage -> {
                    String fileUrl = "http://localhost:8080/files/" + relativePath;
                    long fileSize = resizedImage.length;
                    String fileHash = DigestUtils.sha256Hex(resizedImage);
                    String uploadedBy = HttpRequestManager.getUserId();

                    FileMetadata fileMetadata = FileMetadata.builder().id(UUID.randomUUID()).originalName(originalName).storedName(storedName)
                            .contentType(contentType).size(fileSize).url(fileUrl).uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now())
                            .accessLevel("public").fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return saveFileLocally(resizedImage, filePath)
                            .then(fileMetadataRepository.save(fileMetadata))
                            .map(savedMeta -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, "image", fileSize, preset.name()))
                            .onErrorResume(localEx -> fileMetadataRepository.deleteById(fileMetadata.getId()).then(Mono.error(localEx)));
                })
                .onErrorResume(e -> {
                    log.error("Error processing and saving image: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    private Mono<FileResponseDto> processAndSaveVideo(FilePart file) {
        String originalName = file.filename();
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = originalName;
        String relativePath = "videos/" + storedName;
        Path filePath = Path.of(uploadRootFolder, relativePath);

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .collectList()
                .map(this::joinByteBuffers)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(fileBytes -> {
                    String fileUrl = "http://localhost:8080/files/" + relativePath; // Mock URL
                    long fileSize = fileBytes.length;
                    String fileHash = DigestUtils.sha256Hex(fileBytes);
                    String uploadedBy = HttpRequestManager.getUserId();

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .id(UUID.randomUUID()).originalName(originalName).storedName(storedName)
                            .contentType(contentType).size(fileSize).url(fileUrl)
                            .uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now())
                            .accessLevel("public").fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return saveFileLocally(fileBytes, filePath)
                            .then(fileMetadataRepository.save(fileMetadata))
                            .map(savedMeta -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, "video", fileSize, "original"))
                            .onErrorResume(localEx -> fileMetadataRepository.deleteById(fileMetadata.getId()).then(Mono.error(localEx)));
                })
                .onErrorResume(e -> {
                    log.error("Error processing and saving video: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    private Mono<FileResponseDto> processAndSaveFile(FilePart file) {
        String originalName = file.filename();
        String fileFormat = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".") + 1) : "unknown";
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = originalName;
        String relativePath = "files/" + storedName;
        Path filePath = Path.of(uploadRootFolder, relativePath);

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .collectList()
                .map(this::joinByteBuffers)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(fileBytes -> {
                    String fileUrl = "http://localhost:8080/files/" + relativePath; // Mock URL
                    long fileSize = fileBytes.length;
                    String fileHash = DigestUtils.sha256Hex(fileBytes);
                    String uploadedBy = HttpRequestManager.getUserId();

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .id(UUID.randomUUID()).originalName(originalName).storedName(storedName)
                            .contentType(contentType).size(fileSize).url(fileUrl)
                            .uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now())
                            .accessLevel("public").fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return saveFileLocally(fileBytes, filePath)
                            .then(fileMetadataRepository.save(fileMetadata))
                            .map(savedMeta -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, fileFormat, fileSize, "original"))
                            .onErrorResume(localEx -> fileMetadataRepository.deleteById(fileMetadata.getId()).then(Mono.error(localEx)));
                })
                .onErrorResume(e -> {
                    log.error("Error processing and saving file: {}", e.getMessage(), e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    /**
     * Helper method to save a file locally in a non-blocking manner.
     * This operation is scheduled on a dedicated thread pool to avoid blocking the event loop.
     *
     * @param data The byte array of the file to save.
     * @param filePath The Path where the file should be saved.
     * @return A Mono<Void> that completes when the file is saved.
     */
    private Mono<Void> saveFileLocally(byte[] data, Path filePath) {
        return Mono.fromRunnable(() -> {
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file: " + filePath, e);
            }
        });
    }

    /**
     * Helper method to join a list of byte arrays into a single byte array.
     *
     * @param byteBuffers The list of byte arrays from a Flux<DataBuffer>.
     * @return A single byte array.
     */
    private byte[] joinByteBuffers(List<byte[]> byteBuffers) {
        int totalLength = byteBuffers.stream().mapToInt(bytes -> bytes.length).sum();
        ByteBuffer combinedBuffer = ByteBuffer.allocate(totalLength);
        byteBuffers.forEach(combinedBuffer::put);
        return combinedBuffer.array();
    }
}