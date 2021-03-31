/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class BlackDuckRedirectStrategy implements RedirectStrategy {
    private LaxRedirectStrategy baseRedirectStrategy = new LaxRedirectStrategy();

    @Override
    public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status == 308) {
            return true;
        } else {
            return baseRedirectStrategy.isRedirected(httpRequest, httpResponse, httpContext);
        }
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status == 308) {
            URI uri = baseRedirectStrategy.getLocationURI(httpRequest, httpResponse, httpContext);
            return RequestBuilder.copy(httpRequest).setUri(uri).build();
        } else {
            return baseRedirectStrategy.getRedirect(httpRequest, httpResponse, httpContext);
        }
    }

}
