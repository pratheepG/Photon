package com.photon.identity.onboarding.entity;

import com.photon.identity.onboarding.enums.ScenarioSubmissionState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "onboarding_form_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingFormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String scenarioId;

    @Column(nullable = false)
    private String formId;

    private int step; // Which step this form belongs to

    @Enumerated(EnumType.STRING)
    private ScenarioSubmissionState submissionState;

    @Column(columnDefinition = "TEXT")
    private String formData; // JSON content (Map<String,Object> or ObjectNode)

    private boolean isFinalStep;

    private Instant submittedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = Instant.now();
    }
}
