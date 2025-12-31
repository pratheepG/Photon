package com.photon.identity.idp.utils;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.StringWriter;

public class PemUtils {
    public static String toPem(Object object) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(object);
        }
        return stringWriter.toString();
    }
}