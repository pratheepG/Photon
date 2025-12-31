package com.photon.identity.onboarding.repository;

import com.photon.identity.onboarding.entity.OnboardingFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingFormSubmissionRepository extends JpaRepository<OnboardingFormSubmission, String> {
    List<OnboardingFormSubmission> findAllBySessionIdOrderByStep(String sessionId);
    Optional<OnboardingFormSubmission> findBySessionIdAndFormId(String sessionId, String formId);
}