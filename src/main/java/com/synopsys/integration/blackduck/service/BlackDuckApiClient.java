/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingBiFunction;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckApiClient {
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import");
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries");
    public static final BlackDuckPath UPLOADS_PATH = new BlackDuckPath("/api/uploads");
    public static final BlackDuckPath SCAN_DATA_PATH = new BlackDuckPath("/api/scan/data/"); // TODO: Determine why this endpoint requires a slash at the end.
    public static final BlackDuckPath SCAN_DEVELOPER_MODE_PATH = new BlackDuckPath("/api/developer-scans");

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final HttpUrl blackDuckBaseUrl;
    private final Gson gson;
    private final BlackDuckRequestFactory blackDuckRequestFactory;

    public BlackDuckApiClient(BlackDuckHttpClient blackDuckHttpClient, Gson gson, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
        BlackDuckResponsesTransformer blackDuckResponsesTransformer, BlackDuckRequestFactory blackDuckRequestFactory) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckBaseUrl = blackDuckHttpClient.getBaseUrl();
        this.gson = gson;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.blackDuckResponseTransformer = blackDuckResponseTransformer;
        this.blackDuckResponsesTransformer = blackDuckResponsesTransformer;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
    }

    public <T extends BlackDuckResponse> T transformResponse(Response response, Class<T> clazz) throws IntegrationException {
        return blackDuckJsonTransformer.getResponse(response, clazz);
    }

    public String convertToJson(Object obj) {
        return gson.toJson(obj);
    }

    public HttpUrl getUrl(BlackDuckPath path) throws IntegrationException {
        return fullBlackDuckUrl(path);
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getSomeMatchingResponses(blackDuckPathMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder(), predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getBlackDuckPathResponses(blackDuckPathMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, predicate, totalLimit));
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckPathMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        return getBlackDuckPathResponses(blackDuckPathMultipleResponses, requestBuilder, blackDuckResponsesTransformer::getAllResponses);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckPathMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder, int totalLimit) throws IntegrationException {
        return getBlackDuckPathResponses(blackDuckPathMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit));
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(blackDuckPathMultipleResponses.getBlackDuckPath());
        Class<T> responseClass = blackDuckPathMultipleResponses.getResponseClass();
        return getPageResponse(url, responseClass, blackDuckPageDefinition);
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(blackDuckPathSingleResponse.getBlackDuckPath());
        requestBuilder.url(url);
        return blackDuckResponseTransformer.getResponse(requestBuilder.build(), blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        return getResponse(blackDuckPathSingleResponse, blackDuckRequestFactory.createCommonGetRequestBuilder());
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckView
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getSomeMatchingResponses(blackDuckView, linkMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder(), predicate, totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeMatchingResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder, Predicate<T> predicate, int totalLimit)
        throws IntegrationException {
        return getBlackDuckViewResponses(blackDuckView, linkMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, predicate, totalLimit));
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckView, linkMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        return getBlackDuckViewResponses(blackDuckView, linkMultipleResponses, requestBuilder, blackDuckResponsesTransformer::getAllResponses);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckView, linkMultipleResponses, blackDuckRequestFactory.createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder, int totalLimit) throws IntegrationException {
        return getBlackDuckViewResponses(blackDuckView, linkMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit));
    }

    public <T extends BlackDuckResponse> Optional<T> getResponse(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        try {
            HttpUrl url = blackDuckView.getFirstLink(linkSingleResponse.getLink());
            Request request = blackDuckRequestFactory.createCommonGetRequest(url);
            return Optional.of(blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass()));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(HttpUrl url, Class<T> responseClass, int totalLimit) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(HttpUrl url, Class<T> responseClass, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return getPageResponse(requestBuilder, responseClass, blackDuckPageDefinition);
    }

    // ------------------------------------------------
    // getting responses from a Request.Builder
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass) throws IntegrationException {
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponse(BlackDuckRequestBuilder requestBuilder, Class<T> responseClass, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        PagedRequest pagedRequest = new PagedRequest(requestBuilder, blackDuckPageDefinition);
        return blackDuckResponsesTransformer.getOnePageOfResponses(pagedRequest, responseClass);
    }

    public <T extends BlackDuckResponse> T getResponse(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        Request request = blackDuckRequestFactory.createCommonGetRequest(url);
        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // getting responses from a LinkSingleResponse
    // ------------------------------------------------

    /**
     * @deprecated This uses LinkSingleResponse incorrectly. Please use getResponse(HttpUrl url, Class<T> responseClass) instead.
     */
    @Deprecated
    public <T extends BlackDuckResponse> T getResponse(LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        HttpUrl url = new HttpUrl(linkSingleResponse.getLink());
        Request request = blackDuckRequestFactory.createCommonGetRequest(url);
        return blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public HttpUrl post(BlackDuckPath blackDuckPath, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(blackDuckPath);
        return post(url, blackDuckComponent);
    }

    public HttpUrl post(HttpUrl url, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        String json = gson.toJson(blackDuckComponent);
        Request request = blackDuckRequestFactory.createCommonPostRequestBuilder(url, json).build();
        return executePostRequestAndRetrieveURL(request);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(BlackDuckView blackDuckView) throws IntegrationException {
        HttpUrl url = blackDuckView.getHref();
        delete(url);
    }

    public void delete(HttpUrl url) throws IntegrationException {
        Request request = new Request.Builder(url, HttpMethod.DELETE).build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic put
    // ------------------------------------------------
    public void put(BlackDuckView blackDuckView) throws IntegrationException {
        HttpUrl url = blackDuckView.getHref();
        // add the 'missing' pieces back from view that could have been lost
        String json = blackDuckJsonTransformer.producePatchedJson(blackDuckView);
        Request request = blackDuckRequestFactory.createCommonPutRequestBuilder(url, json).build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(HttpUrl url) throws IntegrationException {
        Request request = blackDuckRequestFactory.createCommonGetRequest(url);
        return execute(request);
    }

    public Response get(BlackDuckPath path) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(path);
        Request request = blackDuckRequestFactory.createCommonGetRequest(url);
        return execute(request);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response execute(BlackDuckPath path, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(path);
        requestBuilder.url(url);
        Request request = requestBuilder.build();
        return execute(request);
    }

    public Response execute(Request request) throws IntegrationException {
        Response response = blackDuckHttpClient.execute(request);
        blackDuckHttpClient.throwExceptionForError(response);
        return response;
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public HttpUrl executePostRequestAndRetrieveURL(BlackDuckPath path, BlackDuckRequestBuilder requestBuilder) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(path);
        requestBuilder.url(url);
        return executePostRequestAndRetrieveURL(requestBuilder.build());
    }

    public HttpUrl executePostRequestAndRetrieveURL(Request request) throws IntegrationException {
        try (Response response = execute(request)) {
            return new HttpUrl(response.getHeaderValue("location"));
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> List<T> getBlackDuckPathResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder requestBuilder,
        ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        HttpUrl url = fullBlackDuckUrl(blackDuckPathMultipleResponses.getBlackDuckPath());
        Class<T> responseClass = blackDuckPathMultipleResponses.getResponseClass();
        return getSpecialResponses(url, responseClass, requestBuilder, responsesTransformer);
    }

    private <T extends BlackDuckResponse> List<T> getBlackDuckViewResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder requestBuilder,
        ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        HttpUrl url = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        Class<T> responseClass = linkMultipleResponses.getResponseClass();
        return getSpecialResponses(url, responseClass, requestBuilder, responsesTransformer);
    }

    private <T extends BlackDuckResponse> List<T> getSpecialResponses(HttpUrl httpUrl, Class<T> responseClass, BlackDuckRequestBuilder requestBuilder,
        ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        requestBuilder.url(httpUrl);
        return responsesTransformer.apply(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    private HttpUrl fullBlackDuckUrl(BlackDuckPath blackDuckPath) throws IntegrationException {
        return blackDuckPath.getFullBlackDuckUrl(blackDuckBaseUrl);
    }

}
