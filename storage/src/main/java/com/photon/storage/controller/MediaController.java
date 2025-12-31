package com.photon.storage.controller;

import com.photon.dto.ApiResponseDto;

import com.photon.endpoint.annotation.ActionInfo;
import com.photon.endpoint.annotation.FeatureInfo;
import com.photon.endpoint.enums.AccessLevel;
import com.photon.storage.dto.FileResponseDto;
import com.photon.storage.service.api.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/media")
@FeatureInfo(id = "MEDIA", name = "Media", description = "Media API")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload-images")
    @ActionInfo(id = "UPLOAD_IMAGES", name = "Upload images", accessLevel = AccessLevel.VIEWER, description = "Upload images and get the CDN endpoint for the uploaded images")
    public Mono<ResponseEntity<ApiResponseDto<List<FileResponseDto>>>> uploadImages(@RequestPart("files") Flux<FilePart> files, @RequestParam(value = "presets", required = false) List<String> presetNames, ServerHttpRequest serverHttpRequest) {
        return mediaService.uploadImages(files, presetNames != null ? presetNames : List.of("original"), serverHttpRequest).map(listApiResponseDto -> ResponseEntity.status(HttpStatus.CREATED).body(listApiResponseDto));
    }

    @PostMapping("/upload-videos")
    @ActionInfo(id = "UPLOAD_VIDEOS", name = "Upload videos", accessLevel = AccessLevel.VIEWER, description = "Upload videos and get the CDN endpoint for the uploaded videos")
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadVideos(@RequestPart("files") Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return mediaService.uploadVideos(files, serverHttpRequest);
    }

    @PostMapping("/upload-files")
    @ActionInfo(id = "UPLOAD_FILES", name = "Upload files", accessLevel = AccessLevel.VIEWER, description = "Upload files and get the CDN endpoint for the uploaded files")
    public Mono<ApiResponseDto<List<FileResponseDto>>> uploadFiles(@RequestPart("files") Flux<FilePart> files, ServerHttpRequest serverHttpRequest) {
        return mediaService.uploadFiles(files, serverHttpRequest);
    }
}