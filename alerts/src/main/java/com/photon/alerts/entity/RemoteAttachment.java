package com.photon.alerts.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("remote_attachment")
public class RemoteAttachment {

    @Id
    private Long id;

    @Column("template_id")
    private Long templateId;

    @Column("file_name")
    private String fileName;

    @Column("download_url")
    private String downloadUrl;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("modified_at")
    private LocalDateTime modifiedAt;
}