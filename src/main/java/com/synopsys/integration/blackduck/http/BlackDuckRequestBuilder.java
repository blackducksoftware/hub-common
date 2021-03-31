/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import static com.synopsys.integration.blackduck.http.BlackDuckRequestFactory.FILTER_PARAMETER;
import static com.synopsys.integration.blackduck.http.BlackDuckRequestFactory.LIMIT_PARAMETER;
import static com.synopsys.integration.blackduck.http.BlackDuckRequestFactory.OFFSET_PARAMETER;
import static com.synopsys.integration.blackduck.http.BlackDuckRequestFactory.Q_PARAMETER;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckRequestBuilder {
    private final Request.Builder requestBuilder;

    public BlackDuckRequestBuilder(Request.Builder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public Request build() {
        return requestBuilder.build();
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public BlackDuckRequestBuilder url(HttpUrl url) {
        requestBuilder.url(url);
        return this;
    }

    public BlackDuckRequestBuilder setBlackDuckPageDefinition(BlackDuckPageDefinition blackDuckPageDefinition) {
        requestBuilder.setQueryParameter(LIMIT_PARAMETER, String.valueOf(blackDuckPageDefinition.getLimit()));
        requestBuilder.setQueryParameter(OFFSET_PARAMETER, String.valueOf(blackDuckPageDefinition.getOffset()));
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckQuery(Optional<BlackDuckQuery> blackDuckQuery) {
        if (blackDuckQuery.isPresent()) {
            requestBuilder.addQueryParameter(Q_PARAMETER, blackDuckQuery.get().getParameter());
        }
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckFilter(BlackDuckRequestFilter blackDuckRequestFilter) {
        if (blackDuckRequestFilter != null) {
            blackDuckRequestFilter.getFilterParameters().forEach(parameter -> {
                requestBuilder.addQueryParameter(FILTER_PARAMETER, parameter);
            });
        }
        return this;
    }

    public BlackDuckRequestBuilder method(HttpMethod method) {
        requestBuilder.method(method);
        return this;
    }

    public BlackDuckRequestBuilder acceptMimeType(String acceptHeader) {
        requestBuilder.acceptMimeType(acceptHeader);
        return this;
    }

    public BlackDuckRequestBuilder bodyEncoding(Charset bodyEncoding) {
        requestBuilder.bodyEncoding(bodyEncoding);
        return this;
    }

    public BlackDuckRequestBuilder queryParameters(Map<String, Set<String>> queryParameters) {
        requestBuilder.queryParameters(queryParameters);
        return this;
    }

    public BlackDuckRequestBuilder addQueryParameter(String key, String value) {
        requestBuilder.addQueryParameter(key, value);
        return this;
    }

    public BlackDuckRequestBuilder headers(Map<String, String> headers) {
        requestBuilder.headers(headers);
        return this;
    }

    public BlackDuckRequestBuilder addHeader(String key, String value) {
        requestBuilder.addHeader(key, value);
        return this;
    }

    public BlackDuckRequestBuilder bodyContent(BodyContent bodyContent) {
        requestBuilder.bodyContent(bodyContent);
        return this;
    }

    public HttpUrl getUrl() {
        return requestBuilder.getUrl();
    }

    public HttpMethod getMethod() {
        return requestBuilder.getMethod();
    }

    public String getAcceptMimeType() {
        return requestBuilder.getAcceptMimeType();
    }

    public Charset getBodyEncoding() {
        return requestBuilder.getBodyEncoding();
    }

    public Map<String, Set<String>> getQueryParameters() {
        return requestBuilder.getQueryParameters();
    }

    public Map<String, String> getHeaders() {
        return requestBuilder.getHeaders();
    }

    public BodyContent getBodyContent() {
        return requestBuilder.getBodyContent();
    }

}
