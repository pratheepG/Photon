package com.photon.console.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sdk_artifact")
public class SdkArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    private String moduleId;
    private String language;
    private String metadataHash;
    private String filename;
    private long sizeBytes;

    /**
     * Binary content (zip). Marked as LOB and explicit columnDefinition bytea for Postgres.
     */
    @Column(name = "content", columnDefinition = "bytea")
    private byte[] content;

    private String createdBy;

    @CreatedDate
    private OffsetDateTime createdAt;
}