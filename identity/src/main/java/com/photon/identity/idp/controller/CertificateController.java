package com.photon.identity.idp.controller;

import com.photon.identity.idp.dto.CertificateDto;
import com.photon.identity.idp.dto.request.CertificateGenerationRequestDto;
import com.photon.identity.idp.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/certs")
public class CertificateController {

    private final CertificateService certificateService;

    @Autowired
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping
    public ResponseEntity<?> createCertificate(@RequestBody CertificateDto certificateDto) {
        return ResponseEntity.ok().body(certificateService.createCertificate(certificateDto));
    }

    @GetMapping
    public ResponseEntity<?> getAllCertificates(@RequestParam(required = false) int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber,pageSize);
        return ResponseEntity.ok().body(this.certificateService.getAllCertificates(page));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCertificate(@RequestParam List<Long> ids) {
        return ResponseEntity.ok().body(this.certificateService.deleteCertificate(ids));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateCertificate(@RequestBody CertificateGenerationRequestDto request) {
        return ResponseEntity.ok().body(this.certificateService.generateCertificate(request));
    }

}