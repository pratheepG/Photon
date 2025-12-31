package com.photon.identity.idp.service;

import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.commons.enums.CertificateSignatureAlgorithm;
import com.photon.identity.idp.dto.CertificateDto;
import com.photon.identity.idp.dto.GeneratedCertificateDto;
import com.photon.identity.idp.dto.mapper.CertificateMapper;
import com.photon.identity.idp.dto.request.CertificateGenerationRequestDto;
import com.photon.identity.idp.entity.Certificate;
import com.photon.identity.idp.repository.CertificateRepository;
import com.photon.identity.idp.utils.CertificateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CertificateService {
    private final CertificateRepository certificateRepository;

    @Autowired
    CertificateService(CertificateRepository certificateRepository){
        this.certificateRepository = certificateRepository;
    }

    public ApiResponseDto<?> generateCertificate(CertificateGenerationRequestDto req) {
        try {
            log.debug("Generating cert with request: {}", req);
            GeneratedCertificateDto generated = CertificateGenerator.generate(req);
            X509Certificate cert = generated.getCertificate();
            Certificate entity = CertificateMapper.toEntity(req, cert, generated);
            certificateRepository.save(entity);
            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (Exception e) {
            log.error("Error generating certificate", e);
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> createCertificate(CertificateDto certificateDto) {
        try {
            log.debug("Certificate Data : {}", certificateDto.toString());
            if(Objects.isNull(CertificateMapper.toEntity(certificateDto).getId()))
                throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
            return SuccessEnum.CREATED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in createCertificate: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in createCertificate: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<List<CertificateDto>> getAllCertificates(Pageable pageable) {
        try {

            Page<CertificateDto> certificates = this.certificateRepository.findAll(pageable).map(CertificateMapper::toResponseDto);
            return SuccessEnum.SUCCESS.getSuccessResponseBody(certificates.stream().toList(), null,
                    pageable.getPageNumber(), certificates.stream().toList().size(),
                    certificates.getTotalPages(), (int) certificates.getTotalElements());

        } catch (ApplicationException ae) {
            log.error("ApplicationException in getAllCertificates: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in getAllCertificates: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDto<?> deleteCertificate(List<Long> ids){
        try {
            this.certificateRepository.deleteAllById(ids);
            if (this.certificateRepository.findAllById(ids).isEmpty())
                throw new ApplicationException(ExceptionEnum.ERR_1004.getErrorResponseBody(), HttpStatus.BAD_REQUEST);
            else
                return SuccessEnum.DELETED.getSuccessResponseBody();
        } catch (ApplicationException ae) {
            log.error("ApplicationException in deleteCertificate: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Exception in deleteCertificate: {}", e.getMessage());
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}