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
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class SignatureScannerClient extends IntHttpClient {
    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

    private Certificate serverCertificate;

    public SignatureScannerClient(BlackDuckHttpClient blackDuckHttpClient) {
        super(blackDuckHttpClient.getLogger(), blackDuckHttpClient.getTimeoutInSeconds(), blackDuckHttpClient.isAlwaysTrustServerCertificate(), blackDuckHttpClient.getProxyInfo());
    }

    public SignatureScannerClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    public SignatureScannerClient(IntLogger logger, int timeoutInSeconds, ProxyInfo proxyInfo, SSLContext sslContext) {
        super(logger, timeoutInSeconds, proxyInfo, sslContext);
    }

    @Override
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.addInterceptorLast(new BlackDuckCertificateInterceptor());
        httpClientBuilder.setConnectionReuseStrategy((httpResponse, httpContext) -> true);
    }

    @Override
    public Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException {
        HttpContext httpContext = new BasicHttpContext();
        Optional<Response> response = super.executeGetRequestIfModifiedSince(getRequest, timeToCheck, httpContext);

        Certificate[] peerCertificates = (Certificate[]) httpContext.getAttribute(PEER_CERTIFICATES);
        if (null != peerCertificates) {
            serverCertificate = peerCertificates[0];
        }

        return response;
    }

    public Certificate getServerCertificate() {
        return serverCertificate;
    }

}
