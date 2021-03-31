/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.keystore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.synopsys.integration.log.IntLogger;

public class KeyStoreHelper {
    private static final char[] DEFAULT_JAVA_KEYSTORE_PASSWORD = new char[] { 'c', 'h', 'a', 'n', 'g', 'e', 'i', 't' };

    private final IntLogger logger;

    public KeyStoreHelper(IntLogger logger) {
        this.logger = logger;
    }

    public void updateKeyStoreWithServerCertificate(String alias, Certificate serverCertificate, String keyStoreFilePath) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream inputStream = new FileInputStream(keyStoreFilePath)) {
                keyStore.load(inputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }

            keyStore.setCertificateEntry(alias, serverCertificate);

            try (OutputStream outputStream = new FileOutputStream(keyStoreFilePath)) {
                keyStore.store(outputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.errorAndDebug("Could not manage the local keystore - communicating to the server will have to be configured manually: " + e.getMessage(), e);
        }
    }

}
