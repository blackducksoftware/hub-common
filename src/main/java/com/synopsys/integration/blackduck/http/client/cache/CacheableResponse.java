/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client.cache;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class CacheableResponse implements Response {
    private final Request request;
    private final Response response;
    private final LRUMap<Request, Response> cache;

    private String stringResponse;

    public CacheableResponse(Request request, Response response, LRUMap<Request, Response> cache) {
        this.request = request;
        this.response = response;
        this.cache = cache;
    }

    @Override
    public HttpUriRequest getRequest() {
        return response.getRequest();
    }

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public boolean isStatusCodeSuccess() {
        return response.isStatusCodeSuccess();
    }

    @Override
    public boolean isStatusCodeError() {
        return response.isStatusCodeError();
    }

    @Override
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    @Override
    public InputStream getContent() throws IntegrationException {
        return response.getContent();
    }

    @Override
    public String getContentString() throws IntegrationException {
        if (null != stringResponse) {
            return stringResponse;
        }

        stringResponse = response.getContentString();
        cache.put(request, this);
        return stringResponse;
    }

    @Override
    public String getContentString(Charset encoding) throws IntegrationException {
        if (null != stringResponse) {
            return stringResponse;
        }

        stringResponse = response.getContentString(encoding);
        cache.put(request, this);
        return stringResponse;
    }

    @Override
    public Long getContentLength() {
        return response.getContentLength();
    }

    @Override
    public String getContentEncoding() {
        return response.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public Map<String, String> getHeaders() {
        return response.getHeaders();
    }

    @Override
    public String getHeaderValue(String name) {
        return response.getHeaderValue(name);
    }

    @Override
    public CloseableHttpResponse getActualResponse() {
        return response.getActualResponse();
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    @Override
    public long getLastModified() throws IntegrationException {
        return response.getLastModified();
    }

    @Override
    public void throwExceptionForError() throws IntegrationRestException {
        response.throwExceptionForError();
    }

}
