/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;

public interface BlackDuckHttpClient {
    Response execute(Request request) throws IntegrationException;

    Optional<Response> executeGetRequestIfModifiedSince(Request getRequest, long timeToCheck) throws IntegrationException, IOException;

    Response attemptAuthentication() throws IntegrationException;

    boolean isAlreadyAuthenticated(HttpUriRequest request);

    Optional<ErrorResponse> extractErrorResponse(String responseContent);

    void handleErrorResponse(HttpUriRequest request, Response response);

    void throwExceptionForError(Response response) throws IntegrationException;

    HttpUrl getBaseUrl();

    String getUserAgentString();

    HttpClientBuilder getHttpClientBuilder();

    int getTimeoutInSeconds();

    boolean isAlwaysTrustServerCertificate();

    ProxyInfo getProxyInfo();

    IntLogger getLogger();

}
