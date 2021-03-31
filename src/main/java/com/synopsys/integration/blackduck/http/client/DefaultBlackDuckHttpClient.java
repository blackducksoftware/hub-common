/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.util.Optional;

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.useragent.BlackDuckCommon;
import com.synopsys.integration.blackduck.useragent.UserAgentBuilder;
import com.synopsys.integration.blackduck.useragent.UserAgentItem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.NameVersion;

public abstract class DefaultBlackDuckHttpClient extends AuthenticatingIntHttpClient implements BlackDuckHttpClient {
    private final HttpUrl baseUrl;
    private final BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery;
    private final String userAgentString;

    protected final AuthenticationSupport authenticationSupport;

    public DefaultBlackDuckHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport,
        BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        this(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl, new UserAgentItem(solutionDetails), BlackDuckCommon.createUserAgentItem(), authenticationSupport, blackDuckMediaTypeDiscovery);
    }

    public DefaultBlackDuckHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, UserAgentItem solutionUserAgentItem, AuthenticationSupport authenticationSupport,
        BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        this(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl, solutionUserAgentItem, BlackDuckCommon.createUserAgentItem(), authenticationSupport, blackDuckMediaTypeDiscovery);
    }

    public DefaultBlackDuckHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, UserAgentItem solutionUserAgentItem, UserAgentItem blackDuckCommonUserAgentItem,
        AuthenticationSupport authenticationSupport, BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);

        if (null == baseUrl) {
            throw new IllegalArgumentException("No base url was provided.");
        }

        this.baseUrl = baseUrl;
        this.blackDuckMediaTypeDiscovery = blackDuckMediaTypeDiscovery;

        UserAgentBuilder userAgentBuilder = new UserAgentBuilder();
        userAgentBuilder.addUserAgent(solutionUserAgentItem);
        userAgentBuilder.addUserAgent(blackDuckCommonUserAgentItem);
        this.userAgentString = userAgentBuilder.createFullUserAgentString();

        this.authenticationSupport = authenticationSupport;
    }

    @Override
    public Response execute(Request request) throws IntegrationException {
        Request.Builder requestBuilder = request.createBuilder();
        HttpUrl httpUrl = requestBuilder.getUrl();
        String mediaType = requestBuilder.getAcceptMimeType();

        String replacementMediaType = blackDuckMediaTypeDiscovery.determineMediaType(httpUrl, mediaType);
        requestBuilder.acceptMimeType(replacementMediaType);

        if (!requestBuilder.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
            requestBuilder.addHeader(HttpHeaders.USER_AGENT, userAgentString);
        }

        request = requestBuilder.build();

        try {
            return super.execute(request);
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return authenticationSupport.isTokenAlreadyAuthenticated(request);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleTokenErrorResponse(this, request, response);
    }

    @Override
    public void throwExceptionForError(Response response) throws IntegrationException {
        try {
            response.throwExceptionForError();
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    @Override
    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public HttpClientBuilder getHttpClientBuilder() {
        return getClientBuilder();
    }

    @Override
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.setRedirectStrategy(new BlackDuckRedirectStrategy());
    }

    private IntegrationException transformException(IntegrationRestException e) {
        String httpResponseContent = e.getHttpResponseContent();
        Optional<ErrorResponse> optionalErrorResponse = extractErrorResponse(httpResponseContent);
        // Not all IntegrationRestExceptions are from Black Duck - if we were able to
        // transform the IntegrationRestException, we want to return the resulting
        // BlackDuckApiException, otherwise, we want to ignore any potential
        // transformation and just return the original IntegrationRestException
        if (optionalErrorResponse.isPresent()) {
            ErrorResponse errorResponse = optionalErrorResponse.get();
            String apiExceptionErrorMessage = String.format("%s [HTTP Error]: %s", errorResponse.getErrorMessage(), e.getMessage());
            return new BlackDuckApiException(e, apiExceptionErrorMessage, errorResponse.getErrorCode());
        } else {
            return e;
        }
    }

}
