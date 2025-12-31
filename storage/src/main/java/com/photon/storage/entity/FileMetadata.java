package com.photon.storage.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_metadata")
public class FileMetadata {
    @Id
    private UUID id;
    private String originalName;
    private String storedName;
    private String contentType;
    private long size;
    private String url;
    private String uploadedBy;
    private String modifiedBy;
    private Instant uploadedAt;
    private String accessLevel;
    private String fileHash;
    private String associatedEntityId;
    private boolean isDeleted;
}