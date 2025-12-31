package com.photon.identity.onboarding.entity;

import com.photon.identity.onboarding.enums.ApprovalType;
import com.photon.identity.onboarding.enums.ScenarioSubmissionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scenario")
public class Scenario {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false)
    private ScenarioSubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_type", nullable = false)
    private ApprovalType approvalType;

}