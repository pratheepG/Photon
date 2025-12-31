package com.photon.identity.idp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.photon.identity.commons.enums.CertificateSignatureAlgorithm;
import com.photon.identity.idp.entity.Certificate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Certificate}
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificateDto implements Serializable {
    Long id;
    @NotBlank(message = "Certificate Name cannot be blank")
    String certificateName;
    @NotBlank(message = "Keystore password cannot be blank")
    String keystorePassword;
    @NotNull(message = "Keystore content cannot be null")
    byte[] keystore;
    @NotNull(message = "Keystore type cannot be null")
    @NotBlank(message = "Keystore type cannot be blank")
    String keystoreType;
    @NotBlank(message = "Alias cannot be blank")
    String alias;
    @NotBlank(message = "Key password cannot be blank")
    String keyPassword;
    String issuer;
    String subject;
    LocalDateTime validFrom;
    LocalDateTime validTo;
    String serialNumber;
    @NotBlank(message = "Certificate Signature Algorithm cannot be blank")
    CertificateSignatureAlgorithm signatureAlgorithm;
    String certificateType;
    byte[] certificateChain;
    String countryName;
    String stateName;
    String localityName;
    String organizationName;
    String organizationalUnitName;
    String hostName;
    String emailAddress;
}