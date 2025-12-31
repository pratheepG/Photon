package com.photon.content.location.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("locality_import_status")
public class LocalityImportStatus {

    @Id
    private Long id;

    private String jobName;
    private String lastProcessedStateName;
    private Integer processedCount;
    private String status;
    private Instant startedAt;
    private Instant completedAt;

}