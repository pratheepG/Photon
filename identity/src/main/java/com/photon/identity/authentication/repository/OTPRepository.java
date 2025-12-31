package com.photon.identity.authentication.repository;

import com.photon.identity.authentication.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OTPRepository extends JpaRepository<OTP, Long>, JpaSpecificationExecutor<OTP> {
}