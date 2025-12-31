package com.photon.alerts.provider.mail;

import com.photon.alerts.dto.DownloadedAttachmentDto;
import com.photon.alerts.dto.RemoteAttachmentDto;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class HelperMethods {

    private static final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .build();

    public static Mono<DownloadedAttachmentDto> downloadAttachment(RemoteAttachmentDto attachment) {
        return webClient.get()
                .uri(attachment.getDownloadUrl())
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .collectList()
                .flatMap(buffers -> {
                    int totalSize = buffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                    byte[] bytes = new byte[totalSize];
                    int offset = 0;
                    for (DataBuffer buffer : buffers) {
                        int len = buffer.readableByteCount();
                        buffer.read(bytes, offset, len);
                        offset += len;
                        DataBufferUtils.release(buffer);
                    }
                    return Mono.just(new DownloadedAttachmentDto(Objects.requireNonNullElse(attachment.getFileName(), "file"), bytes));
                });
    }
}