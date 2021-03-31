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
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectGetService extends DataService {
    public ProjectGetService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectView> allProjectItems = blackDuckApiClient.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectView> projectItems = blackDuckApiClient.getSomeResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder, limit);
        return projectItems;
    }

    public Optional<ProjectView> getProjectViewByProjectName(String projectName) throws IntegrationException {
        List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.getName())) {
                return Optional.of(project);
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getProjectVersionViewByProjectVersionName(ProjectView projectView, String projectVersionName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("versionName", projectVersionName);
        BlackDuckRequestBuilder requestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        Predicate<ProjectVersionView> predicate = projectVersionView -> projectVersionName.equals(projectVersionView.getVersionName());

        return blackDuckApiClient.getSomeMatchingResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder, predicate, 1)
                   .stream()
                   .findFirst();
    }

}
