package com.photon.identity.onboarding.entity;

import com.photon.identity.onboarding.enums.ScenarioSubmissionState;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scenario_form")
public class ScenarioForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "step")
    private int step;

    @Column(name = "scenario_id")
    private String scenarioId;

    @Column(name = "form_id")
    private String formId;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_state")
    private ScenarioSubmissionState submissionState;

}