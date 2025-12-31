package com.photon.storage.service.api;

import com.photon.dto.ApiResponseDto;
import com.photon.storage.dto.FileResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MediaService {
    Mono<ApiResponseDto<List<FileResponseDto>>> uploadImages(Flux<FilePart> files, List<String> presetNames, ServerHttpRequest serverHttpRequest);
    Mono<ApiResponseDto<List<FileResponseDto>>> uploadVideos(Flux<FilePart> files, ServerHttpRequest serverHttpRequest);
    Mono<ApiResponseDto<List<FileResponseDto>>> uploadFiles(Flux<FilePart> files, ServerHttpRequest serverHttpRequest);
}