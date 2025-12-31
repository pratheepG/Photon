package com.photon.identity.idp.dto;

import lombok.*;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedCertificateDto {
    private X509Certificate certificate;
    private byte[] keystoreBytes;
    private PrivateKey privateKey;
}
