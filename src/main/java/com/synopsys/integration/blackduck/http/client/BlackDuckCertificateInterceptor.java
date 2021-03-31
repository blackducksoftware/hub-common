/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.io.IOException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

public class BlackDuckCertificateInterceptor implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
        if (routedConnection.isOpen()) {
            SSLSession sslSession = routedConnection.getSSLSession();
            if (sslSession != null) {
                Certificate[] certificates = sslSession.getPeerCertificates();
                context.setAttribute(SignatureScannerClient.PEER_CERTIFICATES, certificates);
            }
        }
    }

}
