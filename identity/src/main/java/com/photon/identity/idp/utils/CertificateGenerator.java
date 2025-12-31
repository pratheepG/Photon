package com.photon.identity.idp.utils;

import com.photon.identity.idp.dto.GeneratedCertificateDto;
import com.photon.identity.idp.dto.request.CertificateGenerationRequestDto;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateGenerator {

    public static GeneratedCertificateDto generate(CertificateGenerationRequestDto req) throws Exception {
        String dn = buildDistinguishedName(req);
        KeyPair keyPair = generateKeyPair();
        X509Certificate cert = generateSelfSignedCertificate(keyPair, dn, req.getValidityDays());
        byte[] keystoreBytes = createKeystoreBytes(
                req.getKeystoreType(),
                req.getAlias(),
                req.getKeystorePassword(),
                req.getKeyPassword(),
                keyPair,
                cert
        );

        GeneratedCertificateDto generated = new GeneratedCertificateDto();
        generated.setCertificate(cert);
        generated.setKeystoreBytes(keystoreBytes);
        generated.setPrivateKey(keyPair.getPrivate());
        return generated;
    }

    public static String buildDistinguishedName(CertificateGenerationRequestDto req) {
        return String.format(
                "C=%s, ST=%s, L=%s, O=%s, OU=%s, CN=%s, EMAILADDRESS=%s",
                req.getCountryName(),
                req.getStateName(),
                req.getLocalityName(),
                req.getOrganizationName(),
                req.getOrganizationalUnitName(),
                req.getHostName(),
                req.getEmailAddress()
        );
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static X509Certificate generateSelfSignedCertificate(
            KeyPair keyPair,
            String dn,
            int validityDays
    ) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + (long) validityDays * 24 * 60 * 60 * 1000);

        X500Name issuer = new X500Name(dn);
        BigInteger serialNumber = BigInteger.valueOf(now);

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                startDate,
                endDate,
                issuer,
                keyPair.getPublic()
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
    }

    public static byte[] createKeystoreBytes(
            String keystoreType,
            String alias,
            String keystorePassword,
            String keyPassword,
            KeyPair keyPair,
            X509Certificate cert
    ) throws Exception {
        KeyStore keystore = KeyStore.getInstance(keystoreType);
        keystore.load(null, null);

        Certificate[] certChain = new Certificate[]{ cert };
        keystore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword.toCharArray(), certChain);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            keystore.store(baos, keystorePassword.toCharArray());
            return baos.toByteArray();
        }
    }

}