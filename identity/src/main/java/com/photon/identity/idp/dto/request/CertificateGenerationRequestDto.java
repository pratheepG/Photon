package com.photon.identity.idp.dto.request;

import com.photon.identity.commons.enums.CertificateSignatureAlgorithm;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CertificateGenerationRequestDto {
    private String certificateName;
    private String keystorePassword;
    private String keyPassword;
    private String keystoreType; // JKS / PKCS12
    private String alias;
    private String countryName;
    private String stateName;
    private String localityName;
    private String organizationName;
    private String organizationalUnitName;
    private String hostName;
    private String certificateType;
    private String emailAddress;
    private int validityDays;
    private CertificateSignatureAlgorithm signatureAlgorithm;
}