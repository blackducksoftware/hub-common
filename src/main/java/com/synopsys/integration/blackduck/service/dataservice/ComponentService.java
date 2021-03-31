/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.deprecated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionRemediatingView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.http.BlackDuckMediaTypes;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class ComponentService extends DataService {
    public static final String REMEDIATING_LINK = "remediating";
    public static final LinkSingleResponse<RemediationOptionsView> REMEDIATION_OPTIONS_LINK_RESPONSE = new LinkSingleResponse<>("remediating", RemediationOptionsView.class);

    public static final Function<List<ComponentsView>, Optional<ComponentsView>> FIRST_OR_EMPTY_RESULT =
        (list) ->
            Optional.ofNullable(list)
                .filter(notEmptyList -> notEmptyList.size() > 0)
                .map(notEmptyList -> notEmptyList.get(0));

    public static final Function<List<ComponentsView>, Optional<ComponentsView>> SINGLE_OR_EMPTY_RESULT =
        (list) ->
            Optional.ofNullable(list)
                .filter(notEmptyList -> notEmptyList.size() == 1)
                .map(listOfSingleElement -> listOfSingleElement.get(0));

    public ComponentService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<ComponentsView> getAllSearchResults(ExternalId externalId) throws IntegrationException {
        String forge = externalId.getForge().getName();
        String originId = externalId.createExternalId();
        String componentQuery = String.format("%s|%s", forge, originId);
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("id", componentQuery);

        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        List<ComponentsView> allSearchResults = blackDuckApiClient.getAllResponses(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestBuilder);
        return allSearchResults;
    }

    public Optional<ComponentsView> getSingleOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), SINGLE_OR_EMPTY_RESULT);
    }

    public Optional<ComponentsView> getFirstOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), FIRST_OR_EMPTY_RESULT);
    }

    public <T> T getFilteredSearchResults(List<ComponentsView> searchResults, Function<List<ComponentsView>, T> filterFunction) {
        return filterFunction.apply(searchResults);
    }

    public <T> T getFilteredSearchResults(ExternalId externalId, List<ComponentsView> searchResults, BiFunction<ExternalId, List<ComponentsView>, T> filterFunction) {
        return filterFunction.apply(externalId, searchResults);
    }

    public Optional<ComponentVersionView> getComponentVersionView(ComponentsView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            HttpUrl url = new HttpUrl(searchResult.getVersion());
            return Optional.ofNullable(blackDuckApiClient.getResponse(url, ComponentVersionView.class));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ComponentView> getComponentView(ComponentsView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            HttpUrl url = new HttpUrl(searchResult.getVersion());
            return Optional.ofNullable(blackDuckApiClient.getResponse(url, ComponentView.class));
        } else {
            return Optional.empty();
        }
    }

    public ComponentVersionVulnerabilities getComponentVersionVulnerabilities(ComponentVersionView componentVersion) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder()
                                                     .acceptMimeType(BlackDuckMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        List<VulnerabilityView> vulnerabilityList = blackDuckApiClient.getAllResponses(componentVersion, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE, requestBuilder);
        return new ComponentVersionVulnerabilities(componentVersion, vulnerabilityList);
    }

    public Optional<ComponentVersionUpgradeGuidanceView> getUpgradeGuidance(ComponentVersionView componentVersionView) throws IntegrationException {
        return blackDuckApiClient.getResponse(componentVersionView, ComponentVersionView.UPGRADE_GUIDANCE_LINK_RESPONSE);
    }

    @Deprecated
    /**
     * @deprecated ComponentVersionRemediatingView is no longer available as of Black Duck 2020.10.0
     */
    public boolean canRetrieveRemediationInformation(ComponentVersionView componentVersionView) {
        try {
            simplyRetrieveRemediationInformation(componentVersionView);
            return true;
        } catch (IntegrationException e) {
            return false;
        }
    }

    @Deprecated
    /**
     * @deprecated ComponentVersionRemediatingView is no longer available as of Black Duck 2020.10.0
     */
    public Optional<ComponentVersionRemediatingView> getRemediationInformation(ComponentVersionView componentVersionView) throws IntegrationException {
        if (canRetrieveRemediationInformation(componentVersionView)) {
            return simplyRetrieveRemediationInformation(componentVersionView);
        }

        return Optional.empty();
    }

    private Optional<ComponentVersionRemediatingView> simplyRetrieveRemediationInformation(ComponentVersionView componentVersionView) throws IntegrationException {
        HttpUrl remediatingUrl = componentVersionView.getHref().appendRelativeUrl(ComponentService.REMEDIATING_LINK);
        return Optional.ofNullable(blackDuckApiClient.getResponse(remediatingUrl, ComponentVersionRemediatingView.class));
    }

}
