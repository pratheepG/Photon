package com.photon.identity.idp.controller;

import com.photon.identity.idp.dto.CertificateDto;
import com.photon.identity.idp.dto.mapper.CertificateMapper;
import com.photon.identity.idp.repository.CertificateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/.well-known")
public class JwksController {

    private final CertificateRepository certificateRepository;

    public JwksController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() throws Exception {
        List<Map<String, Object>> jwkList = new ArrayList<>();
        List<CertificateDto> certificates = this.certificateRepository.findAll().stream()
                .map(CertificateMapper::toDto).toList();

        for (CertificateDto crt : certificates) {
            String keystoreType = Objects.requireNonNull(crt.getKeystoreType(), "Keystore type cannot be null");
            KeyStore keyStore = KeyStore.getInstance(keystoreType);

            byte[] decodedJks = Objects.requireNonNull(crt.getKeystore(), "Keystore content cannot be null");
            try (InputStream jksInputStream = new ByteArrayInputStream(decodedJks)) {
                keyStore.load(jksInputStream, Objects.requireNonNull(crt.getKeyPassword(), "Keystore password cannot be null").toCharArray());
            }

            Certificate[] certChain = keyStore.getCertificateChain(crt.getAlias());
            if (certChain == null || certChain.length == 0) {
                log.warn("No cert chain found for alias: {}", crt.getAlias());
                continue;
            }
            X509Certificate leafCert = (X509Certificate) certChain[0];
            RSAPublicKey pubKey = (RSAPublicKey) leafCert.getPublicKey();

            String n = Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getModulus().toByteArray());
            String e = Base64.getUrlEncoder().withoutPadding().encodeToString(pubKey.getPublicExponent().toByteArray());
            List<String> x5cList = Arrays.stream(certChain)
                    .map(cert -> {
                        try {
                            return Base64.getEncoder().encodeToString(cert.getEncoded());
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .collect(Collectors.toList());

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            String x5t = Base64.getUrlEncoder().withoutPadding().encodeToString(sha1.digest(leafCert.getEncoded()));

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String x5tS256 = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256.digest(leafCert.getEncoded()));

            Map<String, Object> jwk = new LinkedHashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("alg", "RS256");
            jwk.put("use", "sig");
            jwk.put("kid", String.valueOf(crt.getId()));
            jwk.put("n", n);
            jwk.put("e", e);
            jwk.put("x5c", x5cList);
            jwk.put("x5t", x5t);
            jwk.put("x5t#S256", x5tS256);

            jwkList.add(jwk);
        }
        return Map.of("keys", jwkList);
    }

    @GetMapping("/cert.pem")
    public String certPem(@RequestParam String alias) throws Exception {
        CertificateDto crt = this.certificateRepository.findAll().stream()
                .map(CertificateMapper::toDto).filter(dto -> dto.getAlias().equals(alias))
                .findFirst().orElseThrow(() -> new RuntimeException("Alias not found"));

        String keystoreType = Objects.requireNonNull(crt.getKeystoreType(), "Keystore type cannot be null");
        KeyStore keyStore = KeyStore.getInstance(keystoreType);
        byte[] decodedJks = Objects.requireNonNull(crt.getKeystore(), "Keystore content cannot be null");
        try (InputStream jksInputStream = new ByteArrayInputStream(decodedJks)) {
            keyStore.load(jksInputStream, Objects.requireNonNull(crt.getKeyPassword(), "Keystore password cannot be null").toCharArray());
        }
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(cert.getEncoded()) +
                "\n-----END CERTIFICATE-----";
    }
}