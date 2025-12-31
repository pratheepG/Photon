package com.photon.identity.idp.dto.mapper;

import com.photon.identity.commons.enums.CertificateSignatureAlgorithm;
import com.photon.identity.idp.dto.CertificateDto;
import com.photon.identity.idp.dto.GeneratedCertificateDto;
import com.photon.identity.idp.dto.request.CertificateGenerationRequestDto;
import com.photon.identity.idp.entity.Certificate;

import java.security.cert.X509Certificate;
import java.time.ZoneId;

public class CertificateMapper {

    public static CertificateDto toDto(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        return new CertificateDto(
                certificate.getId(),
                certificate.getCertificateName(),
                certificate.getKeystorePassword(),
                certificate.getKeystore(),
                certificate.getKeystoreType(),
                certificate.getAlias(),
                certificate.getKeyPassword(),
                certificate.getIssuer(),
                certificate.getSubject(),
                certificate.getValidFrom(),
                certificate.getValidTo(),
                certificate.getSerialNumber(),
                certificate.getSignatureAlgorithm(),
                certificate.getCertificateType(),
                certificate.getCertificateChain(),
                certificate.getCountryName(),
                certificate.getStateName(),
                certificate.getLocalityName(),
                certificate.getOrganizationName(),
                certificate.getOrganizationalUnitName(),
                certificate.getHostName(),
                certificate.getEmailAddress()
        );
    }

    public static CertificateDto toResponseDto(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        return new CertificateDto(
                certificate.getId(),
                certificate.getCertificateName(),
                null,
                null,
                certificate.getKeystoreType(),
                certificate.getAlias(),
                null,
                certificate.getIssuer(),
                certificate.getSubject(),
                certificate.getValidFrom(),
                certificate.getValidTo(),
                certificate.getSerialNumber(),
                certificate.getSignatureAlgorithm(),
                certificate.getCertificateType(),
                null,
                certificate.getCountryName(),
                certificate.getStateName(),
                certificate.getLocalityName(),
                certificate.getOrganizationName(),
                certificate.getOrganizationalUnitName(),
                certificate.getHostName(),
                certificate.getEmailAddress()
        );
    }

    public static Certificate toEntity(CertificateDto certificateDto) {
        if (certificateDto == null) {
            return null;
        }

        Certificate certificate = new Certificate();
        certificate.setId(certificateDto.getId());
        certificate.setCertificateName(certificateDto.getCertificateName());
        certificate.setKeystorePassword(certificateDto.getKeystorePassword());
        certificate.setKeystore(certificateDto.getKeystore());
        certificate.setKeystoreType(certificateDto.getKeystoreType());
        certificate.setAlias(certificateDto.getAlias());
        certificate.setKeyPassword(certificateDto.getKeyPassword());
        certificate.setIssuer(certificateDto.getIssuer());
        certificate.setSubject(certificateDto.getSubject());
        certificate.setValidFrom(certificateDto.getValidFrom());
        certificate.setValidTo(certificateDto.getValidTo());
        certificate.setSerialNumber(certificateDto.getSerialNumber());
        certificate.setSignatureAlgorithm(certificateDto.getSignatureAlgorithm());
        certificate.setCertificateType(certificateDto.getCertificateType());
        certificate.setCertificateChain(certificateDto.getCertificateChain());
        certificate.setCountryName(certificateDto.getCountryName());
        certificate.setStateName(certificateDto.getStateName());
        certificate.setLocalityName(certificateDto.getLocalityName());
        certificate.setOrganizationName(certificateDto.getOrganizationName());
        certificate.setOrganizationalUnitName(certificateDto.getOrganizationalUnitName());
        certificate.setHostName(certificateDto.getHostName());
        certificate.setEmailAddress(certificateDto.getEmailAddress());

        return certificate;
    }

    public static Certificate toEntity(CertificateGenerationRequestDto req, X509Certificate cert, GeneratedCertificateDto generated) {
        if (req == null) {
            return null;
        }

        Certificate entity = new Certificate();
        entity.setCertificateName(req.getCertificateName());
        entity.setKeystorePassword(req.getKeystorePassword());
        entity.setKeyPassword(req.getKeyPassword());
        entity.setKeystoreType(req.getKeystoreType());
        entity.setAlias(req.getAlias());
        entity.setKeystore(generated.getKeystoreBytes());
        entity.setIssuer(cert.getIssuerX500Principal().getName());
        entity.setSubject(cert.getSubjectX500Principal().getName());
        entity.setValidFrom(cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        entity.setValidTo(cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        entity.setSerialNumber(cert.getSerialNumber().toString());
        entity.setSignatureAlgorithm(req.getSignatureAlgorithm() != null ? req.getSignatureAlgorithm() : CertificateSignatureAlgorithm.SHA256withRSA);
        entity.setCertificateType(req.getCertificateType());
        entity.setCountryName(req.getCountryName());
        entity.setStateName(req.getStateName());
        entity.setLocalityName(req.getLocalityName());
        entity.setOrganizationName(req.getOrganizationName());
        entity.setOrganizationalUnitName(req.getOrganizationalUnitName());
        entity.setHostName(req.getHostName());
        entity.setEmailAddress(req.getEmailAddress());

        return entity;
    }

    public static Certificate partialUpdate(CertificateDto certificateDto, Certificate certificate) {
        if (certificateDto == null || certificate == null) {
            return certificate;
        }

        if (certificateDto.getId() != null) {
            certificate.setId(certificateDto.getId());
        }
        if (certificateDto.getCertificateName() != null) {
            certificate.setCertificateName(certificateDto.getCertificateName());
        }
        if (certificateDto.getKeystorePassword() != null) {
            certificate.setKeystorePassword(certificateDto.getKeystorePassword());
        }
        if (certificateDto.getKeystore() != null) {
            certificate.setKeystore(certificateDto.getKeystore());
        }
        if (certificateDto.getKeystoreType() != null) {
            certificate.setKeystoreType(certificateDto.getKeystoreType());
        }
        if (certificateDto.getAlias() != null) {
            certificate.setAlias(certificateDto.getAlias());
        }
        if (certificateDto.getKeyPassword() != null) {
            certificate.setKeyPassword(certificateDto.getKeyPassword());
        }
        if (certificateDto.getIssuer() != null) {
            certificate.setIssuer(certificateDto.getIssuer());
        }
        if (certificateDto.getSubject() != null) {
            certificate.setSubject(certificateDto.getSubject());
        }
        if (certificateDto.getValidFrom() != null) {
            certificate.setValidFrom(certificateDto.getValidFrom());
        }
        if (certificateDto.getValidTo() != null) {
            certificate.setValidTo(certificateDto.getValidTo());
        }
        if (certificateDto.getSerialNumber() != null) {
            certificate.setSerialNumber(certificateDto.getSerialNumber());
        }
        if (certificateDto.getSignatureAlgorithm() != null) {
            certificate.setSignatureAlgorithm(certificateDto.getSignatureAlgorithm());
        }
        if (certificateDto.getCertificateType() != null) {
            certificate.setCertificateType(certificateDto.getCertificateType());
        }
        if (certificateDto.getCertificateChain() != null) {
            certificate.setCertificateChain(certificateDto.getCertificateChain());
        }
        if (certificateDto.getCountryName() != null) {
            certificate.setCountryName(certificateDto.getCountryName());
        }
        if (certificateDto.getStateName() != null) {
            certificate.setStateName(certificateDto.getStateName());
        }
        if (certificateDto.getLocalityName() != null) {
            certificate.setLocalityName(certificateDto.getLocalityName());
        }
        if (certificateDto.getOrganizationName() != null) {
            certificate.setOrganizationName(certificateDto.getOrganizationName());
        }
        if (certificateDto.getOrganizationalUnitName() != null) {
            certificate.setOrganizationalUnitName(certificateDto.getOrganizationalUnitName());
        }
        if (certificateDto.getHostName() != null) {
            certificate.setHostName(certificateDto.getHostName());
        }
        if (certificateDto.getEmailAddress() != null) {
            certificate.setEmailAddress(certificateDto.getEmailAddress());
        }

        return certificate;
    }
}