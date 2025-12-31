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
import com.photon.utils.ReactiveHttpRequestManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing media uploads to AWS S3 in a reactive, non-blocking manner.
 * It handles images, videos, and generic files, applying image presets where required.
 */
@Slf4j
public class S3MediaService implements MediaService {

    private final S3AsyncClient s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3MediaService(S3AsyncClient s3Client, FileMetadataRepository fileMetadataRepository) {
        this.s3Client = s3Client;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    /**
     * Uploads a Flux of images to S3, applying specified presets.
     * The method processes each file part reactively and offloads blocking operations.
     *
     * @param files A reactive stream of FilePart objects.
     * @param presetNames List of image preset names to apply.
     * @return A Mono containing an ApiResponseDto with a list of FileResponseDto.
     */
    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadImages(Flux<FilePart> files, List<String> presetNames, ServerHttpRequest serverHttpRequest) {
        List<ImagePreset> presets = convertToPresets(presetNames);
        if (presets == null || presets.isEmpty()) {
            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1040.getErrorResponseBody(), HttpStatus.BAD_REQUEST));
        }

        return ReactiveHttpRequestManager.getUserId(serverHttpRequest).flatMap(uploadedBy ->
                files.flatMap(file -> {
                            String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
                            if (contentType.startsWith("image/")) {
                                return Flux.fromIterable(presets)
                                        .flatMap(preset -> processAndUploadImage(file, preset, uploadedBy));
                            }
                            log.warn("Skipping non-image file: {}", file.filename());
                            return Flux.empty();
                        })
                        .collectList()
                        .map(SuccessEnum.SUCCESS::getSuccessResponseBody)
                        .onErrorResume(e -> {
                            log.error("Error processing and uploading image: {}", e.getMessage(), e);
                            if(e instanceof ApplicationException)
                                return Mono.error(e);
                            return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                        })
        );
    }

    /**
     * Uploads a Flux of videos to S3.
     *
     * @param files A reactive stream of FilePart objects.
     * @return A Mono containing an ApiResponseDto with a list of FileResponseDto.
     */
    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadVideos(Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return ReactiveHttpRequestManager.getUserId(serverHttpRequest).flatMap(uploadedBy ->
                files.flatMap(file -> processAndUploadVideo(file, uploadedBy))
                        .collectList()
                        .map(SuccessEnum.SUCCESS::getSuccessResponseBody)
        );
    }

    /**
     * Uploads a Flux of generic files to S3.
     *
     * @param files A reactive stream of FilePart objects.
     * @return A Mono containing an ApiResponseDto with a list of FileResponseDto.
     */
    @Override
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadFiles(Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return ReactiveHttpRequestManager.getUserId(serverHttpRequest).flatMap(uploadedBy ->
                files.flatMap(file -> processAndUploadFile(file, uploadedBy))
                        .collectList()
                        .map(SuccessEnum.SUCCESS::getSuccessResponseBody)
        );
    }

    /**
     * Converts a list of string preset names to ImagePreset enums.
     *
     * @param presetNames The list of preset names as strings.
     * @return A list of ImagePreset enums, or null if an invalid preset is found.
     */
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

    /**
     * Processes a single image FilePart, resizes it according to the preset, and uploads to S3.
     * The user ID is retrieved reactively before the blocking operations are scheduled.
     *
     * @param file The FilePart to process.
     * @param preset The image preset to apply.
     * @param uploadedBy The ID of the user uploading the file.
     * @return A Mono with the FileResponseDto for the processed and uploaded image.
     */
    private Mono<FileResponseDto> processAndUploadImage(FilePart file, ImagePreset preset, String uploadedBy) {
        String originalName = file.filename();
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = preset.name().toLowerCase() + "-" + originalName;

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
                    String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/images/" + storedName;
                    long fileSize = resizedImage.length;
                    String fileHash = DigestUtils.sha256Hex(resizedImage);

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .originalName(originalName).storedName(storedName).contentType(contentType)
                            .size(fileSize).url(fileUrl).uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now())
                            .accessLevel("public").fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return fileMetadataRepository.save(fileMetadata)
                            .flatMap(savedMeta ->
                                    uploadToS3(ByteBuffer.wrap(resizedImage), "images/" + storedName)
                                            .map(response -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, "image", fileSize, preset.name()))
                                            .onErrorResume(s3ex -> fileMetadataRepository.deleteById(savedMeta.getId()).then(Mono.error(s3ex)))
                            );
                })
                .onErrorResume(e -> {
                    log.error("Error processing and uploading image: {}", e.getMessage(), e);
                    e.printStackTrace();
                    if(e instanceof ApplicationException)
                        return Mono.error(e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    /**
     * Processes and uploads a single video FilePart to S3.
     * The user ID is retrieved reactively before the blocking operations are scheduled.
     *
     * @param file The FilePart to process.
     * @param uploadedBy The ID of the user uploading the file.
     * @return A Mono with the FileResponseDto for the uploaded video.
     */
    private Mono<FileResponseDto> processAndUploadVideo(FilePart file, String uploadedBy) {
        String originalName = file.filename();
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = originalName;

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .collectList()
                .map(this::joinByteBuffers)
                .flatMap(fileBytes -> {
                    String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/videos/" + originalName;
                    long fileSize = fileBytes.length;
                    String fileHash = DigestUtils.sha256Hex(fileBytes);

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .id(UUID.randomUUID()).originalName(originalName).storedName(storedName).contentType(contentType).size(fileSize)
                            .url(fileUrl).uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now()).accessLevel("public")
                            .fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return fileMetadataRepository.save(fileMetadata)
                            .flatMap(savedMeta ->
                                    uploadToS3(ByteBuffer.wrap(fileBytes), "videos/" + storedName)
                                            .map(response -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, "video", fileSize, "original"))
                                            .onErrorResume(s3ex -> fileMetadataRepository.deleteById(savedMeta.getId()).then(Mono.error(s3ex)))
                            );
                })
                .onErrorResume(e -> {
                    log.error("Error processing and uploading video: {}", e.getMessage(), e);
                    if(e instanceof ApplicationException)
                        return Mono.error(e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    /**
     * Processes and uploads a single generic FilePart to S3.
     * The user ID is retrieved reactively before the blocking operations are scheduled.
     *
     * @param file The FilePart to process.
     * @param uploadedBy The ID of the user uploading the file.
     * @return A Mono with the FileResponseDto for the uploaded file.
     */
    private Mono<FileResponseDto> processAndUploadFile(FilePart file, String uploadedBy) {
        String originalName = file.filename();
        String fileFormat = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".") + 1) : "unknown";
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "application/octet-stream";
        String storedName = originalName;

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .collectList()
                .map(this::joinByteBuffers)
                .flatMap(fileBytes -> {
                    String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/files/" + originalName;
                    long fileSize = fileBytes.length;
                    String fileHash = DigestUtils.sha256Hex(fileBytes);

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .id(UUID.randomUUID()).originalName(originalName).storedName(storedName).contentType(contentType)
                            .size(fileSize).url(fileUrl).uploadedBy(uploadedBy).modifiedBy(uploadedBy).uploadedAt(Instant.now())
                            .accessLevel("public").fileHash(fileHash).associatedEntityId("").isDeleted(false).build();

                    return fileMetadataRepository.save(fileMetadata)
                            .flatMap(savedMeta ->
                                    uploadToS3(ByteBuffer.wrap(fileBytes), "files/" + storedName)
                                            .map(response -> new FileResponseDto(savedMeta.getId(), storedName, fileUrl, fileFormat, fileSize, "original"))
                                            .onErrorResume(s3ex -> fileMetadataRepository.deleteById(savedMeta.getId()).then(Mono.error(s3ex)))
                            );
                })
                .onErrorResume(e -> {
                    log.error("Error processing and uploading file: {}", e.getMessage(), e);
                    if(e instanceof ApplicationException)
                        return Mono.error(e);
                    return Mono.error(new ApplicationException(ExceptionEnum.ERR_1039.getErrorResponseBody(), HttpStatus.NOT_ACCEPTABLE));
                });
    }

    /**
     * Uploads a ByteBuffer to a specific key in an S3 bucket.
     *
     * @param fileBuffer The ByteBuffer containing the file data.
     * @param fileKey The key (path) for the object in the S3 bucket.
     * @return A Mono containing the PutObjectResponse from the S3 API call.
     */
    private Mono<PutObjectResponse> uploadToS3(ByteBuffer fileBuffer, String fileKey) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        return Mono.fromFuture(() -> s3Client.putObject(request, AsyncRequestBody.fromByteBuffer(fileBuffer)));
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