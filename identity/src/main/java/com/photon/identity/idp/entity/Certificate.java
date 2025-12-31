package com.photon.identity.idp.entity;

import com.photon.identity.commons.enums.CertificateSignatureAlgorithm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "certificate")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_name", nullable = false)
    @NotBlank(message = "Certificate Name cannot be blank")
    private String certificateName;

    @Column(name = "keystore_password", nullable = false)
    @NotBlank(message = "Keystore password cannot be blank")
    private String keystorePassword;

    @Column(name = "keystore", nullable = false, columnDefinition = "bytea")
    @NotNull(message = "Keystore content cannot be null")
    @NotEmpty(message = "Keystore content cannot be empty")
    private byte[] keystore;

    @Column(name = "keystore_type", nullable = false)
    @NotNull(message = "Keystore type cannot be null")
    @NotBlank(message = "Keystore type cannot be blank")
    private String keystoreType;

    @Column(name = "alias", nullable = false)
    @NotBlank(message = "Alias cannot be blank")
    private String alias;

    @Column(name = "key_password", nullable = false)
    @NotBlank(message = "Key password cannot be blank")
    private String keyPassword;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "subject")
    private String subject;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "serial_number")
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "signature_algorithm")
    private CertificateSignatureAlgorithm signatureAlgorithm;

    @Column(name = "certificate_type")
    private String certificateType; // Set as 'CA' if the PKCS#12 file contains CA certificates, otherwise 'Server'

    @Column(name = "certificate_chain", columnDefinition = "bytea")
    private byte[] certificateChain; // Store base64 encoded chain content here (if applicable)

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "locality_name")
    private String localityName;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "organizational_unit_name")
    private String organizationalUnitName;

    @Column(name = "host_name")
    private String hostName;

    @Column(name = "email_address")
    private String emailAddress;

}