/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionVulnerableBomComponentsView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.VersionBomComponentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class ProjectBomService extends DataService {
    private final ComponentService componentService;

    public ProjectBomService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ComponentService componentService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.componentService = componentService;
    }

    public List<ProjectVersionComponentView> getComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentView> ProjectVersionComponentViews = blackDuckApiClient.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return ProjectVersionComponentViews;
    }

    public List<ProjectVersionVulnerableBomComponentsView> getVulnerableComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionVulnerableBomComponentsView> vulnerableBomComponentViews = blackDuckApiClient.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentView> ProjectVersionComponentViews = getComponentsForProjectVersion(projectVersionView);
        List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (ProjectVersionComponentView projectVersionComponentView : ProjectVersionComponentViews) {
            if (StringUtils.isNotBlank(projectVersionComponentView.getComponentVersion())) {
                HttpUrl projectVersionComponentUrl = new HttpUrl(projectVersionComponentView.getComponentVersion());
                ComponentVersionView componentVersionView = blackDuckApiClient.getResponse(projectVersionComponentUrl, ComponentVersionView.class);
                componentVersionViews.add(componentVersionView);
            }
        }

        List<ComponentVersionVulnerabilities> componentVersionVulnerabilitiesList = new ArrayList<>();
        for (ComponentVersionView componentVersionView : componentVersionViews) {
            ComponentVersionVulnerabilities componentVersionVulnerabilities = componentService.getComponentVersionVulnerabilities(componentVersionView);
            componentVersionVulnerabilitiesList.add(componentVersionVulnerabilities);
        }
        return componentVersionVulnerabilitiesList;
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(ProjectVersionView version) throws IntegrationException {
        List<ProjectVersionComponentView> bomComponents = blackDuckApiClient.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (ProjectVersionComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    public Optional<ProjectVersionPolicyStatusView> getPolicyStatusForVersion(ProjectVersionView version) throws IntegrationException {
        return blackDuckApiClient.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    //TODO investigate what variant is
    public Optional<String> addComponentToProjectVersion(ExternalId componentExternalId, ProjectVersionView projectVersionView) throws IntegrationException {
        HttpUrl projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);
        Optional<ComponentsView> componentSearchResultView = componentService.getFirstOrEmptyResult(componentExternalId);
        String componentVersionUrl = null;
        if (componentSearchResultView.isPresent()) {
            if (StringUtils.isNotBlank(componentSearchResultView.get().getVariant())) {
                componentVersionUrl = componentSearchResultView.get().getVariant();
            } else if (StringUtils.isNotBlank(componentSearchResultView.get().getVersion())) {
                componentVersionUrl = componentSearchResultView.get().getVersion();
            } else {
                componentVersionUrl = componentSearchResultView.get().getComponent();
            }
            addComponentToProjectVersion(new HttpUrl(componentVersionUrl), projectVersionComponentsUrl);
        }

        return Optional.ofNullable(componentVersionUrl);
    }

    public void addProjectVersionToProjectVersion(ProjectVersionView projectVersionViewToAdd, ProjectVersionView targetProjectVersionView) throws IntegrationException {
        HttpUrl toAddUrl = projectVersionViewToAdd.getHref();
        HttpUrl targetUrl = targetProjectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);

        addComponentToProjectVersion(toAddUrl, targetUrl);
    }

    public void addComponentToProjectVersion(ComponentVersionView componentVersionView, ProjectVersionView projectVersionView) throws IntegrationException {
        HttpUrl componentVersionUrl = componentVersionView.getHref();
        HttpUrl projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);

        addComponentToProjectVersion(componentVersionUrl, projectVersionComponentsUrl);
    }

    public void addComponentToProjectVersion(HttpUrl componentVersionUrl, HttpUrl projectVersionComponentsUrl) throws IntegrationException {
        Request request = blackDuckRequestFactory.createCommonPostRequestBuilder(projectVersionComponentsUrl, "{\"component\": \"" + componentVersionUrl.string() + "\"}").build();
        try (Response response = blackDuckApiClient.execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private List<ComponentMatchedFilesView> getMatchedFiles(ProjectVersionComponentView component) throws IntegrationException {
        List<ComponentMatchedFilesView> matchedFiles = new ArrayList<>(0);
        List<ComponentMatchedFilesView> tempMatchedFiles = blackDuckApiClient.getAllResponses(component, ProjectVersionComponentView.MATCHED_FILES_LINK_RESPONSE);
        if (tempMatchedFiles != null && !tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
