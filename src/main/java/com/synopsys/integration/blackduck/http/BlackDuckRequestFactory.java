/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.FileBodyContent;
import com.synopsys.integration.rest.body.MapBodyContent;
import com.synopsys.integration.rest.body.MultipartBodyContent;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckRequestFactory {
    public static final String LIMIT_PARAMETER = "limit";
    public static final String OFFSET_PARAMETER = "offset";
    public static final String Q_PARAMETER = "q";
    public static final String FILTER_PARAMETER = "filter";

    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_OFFSET = 0;

    public Request createCommonGetRequest(HttpUrl url) {
        return createCommonGetRequestBuilder(url).getRequestBuilder().build();
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder() {
        return createCommonGetRequestBuilder(null, Optional.empty(), DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url) {
        return createCommonGetRequestBuilder(url, Optional.empty(), DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(Optional<BlackDuckQuery> blackDuckQuery) {
        return createCommonGetRequestBuilder(null, blackDuckQuery, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(int limit, int offset) {
        return createCommonGetRequestBuilder(null, Optional.empty(), limit, offset);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url, Optional<BlackDuckQuery> blackDuckQuery) {
        return createCommonGetRequestBuilder(url, blackDuckQuery, DEFAULT_LIMIT, DEFAULT_OFFSET);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url, int limit, int offset) {
        return createCommonGetRequestBuilder(url, Optional.empty(), limit, offset);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(Optional<BlackDuckQuery> blackDuckQuery, int limit, int offset) {
        return createCommonGetRequestBuilder(null, blackDuckQuery, limit, offset);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url, Optional<BlackDuckQuery> blackDuckQuery, int limit, int offset) {
        return createCommonGetRequestBuilder(url, blackDuckQuery, null, limit, offset);
    }

    public BlackDuckRequestBuilder createCommonGetRequestBuilder(HttpUrl url, Optional<BlackDuckQuery> blackDuckQuery, BlackDuckRequestFilter blackDuckRequestFilter, int limit, int offset) {
        return createRequestBuilder()
                   .url(url)
                   .addBlackDuckQuery(blackDuckQuery)
                   .addBlackDuckFilter(blackDuckRequestFilter)
                   .setBlackDuckPageDefinition(new BlackDuckPageDefinition(limit, offset));
    }

    public BlackDuckRequestBuilder createCommonPostRequestBuilder(HttpUrl url, File bodyContentFile) {
        return createRequestBuilder()
                   .url(url)
                   .method(HttpMethod.POST)
                   .bodyContent(new FileBodyContent(bodyContentFile));
    }

    public BlackDuckRequestBuilder createCommonPostRequestBuilder(HttpUrl url, Map<String, String> bodyContentMap) {
        return createRequestBuilder()
                   .url(url)
                   .method(HttpMethod.POST)
                   .bodyContent(new MapBodyContent(bodyContentMap));
    }

    public BlackDuckRequestBuilder createCommonPostRequestBuilder(HttpUrl url, String bodyContent) {
        return createRequestBuilder()
                   .url(url)
                   .method(HttpMethod.POST)
                   .bodyContent(new StringBodyContent(bodyContent));
    }

    public BlackDuckRequestBuilder createCommonPostRequestBuilder(Map<String, File> bodyContentFileMap, Map<String, String> bodyContentStringMap) {
        return createRequestBuilder()
                   .method(HttpMethod.POST)
                   .bodyContent(new MultipartBodyContent(bodyContentFileMap, bodyContentStringMap));
    }

    public BlackDuckRequestBuilder createCommonPutRequestBuilder(HttpUrl url, String bodyContent) {
        return createRequestBuilder()
                   .url(url)
                   .method(HttpMethod.PUT)
                   .bodyContent(new StringBodyContent(bodyContent));
    }

    private BlackDuckRequestBuilder createRequestBuilder() {
        return new BlackDuckRequestBuilder(new Request.Builder());
    }

}
