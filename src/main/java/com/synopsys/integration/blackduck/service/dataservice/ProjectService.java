/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class ProjectService extends DataService {
    private final ProjectGetService projectGetService;

    public ProjectService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ProjectGetService projectGetService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.projectGetService = projectGetService;
    }

    public List<ProjectView> getAllProjects() throws IntegrationException {
        return blackDuckApiClient.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
    }

    public ProjectVersionWrapper createProject(ProjectRequest projectRequest) throws IntegrationException {
        HttpUrl projectUrl = blackDuckApiClient.post(ApiDiscovery.PROJECTS_LINK, projectRequest);
        ProjectView projectView = blackDuckApiClient.getResponse(projectUrl, ProjectView.class);
        if (null == projectRequest.getVersionRequest()) {
            return new ProjectVersionWrapper(projectView);
        }

        Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView, projectRequest.getVersionRequest().getVersionName());
        return new ProjectVersionWrapper(projectView, projectVersionView.orElse(null));
    }

    public List<ProjectVersionView> getAllProjectVersions(ProjectView projectView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE);
    }

    public ProjectVersionView createProjectVersion(ProjectView projectView, ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.VERSIONS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a version.", ProjectView.VERSIONS_LINK));
        }
        HttpUrl projectVersionUrl = blackDuckApiClient.post(projectView.getFirstLink(ProjectView.VERSIONS_LINK), projectVersionRequest);
        return blackDuckApiClient.getResponse(projectVersionUrl, ProjectVersionView.class);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        return projectGetService.getAllProjectMatches(projectName);
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        return projectGetService.getProjectMatches(projectName, limit);
    }

    public Optional<ProjectView> getProjectByName(String projectName) throws IntegrationException {
        return projectGetService.getProjectViewByProjectName(projectName);
    }

    public Optional<ProjectVersionView> getProjectVersion(ProjectView project, String projectVersionName) throws IntegrationException {
        return projectGetService.getProjectVersionViewByProjectVersionName(project, projectVersionName);
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(NameVersion projectAndVersion) throws IntegrationException {
        return getProjectVersion(projectAndVersion.getName(), projectAndVersion.getVersion());
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(String projectName, String projectVersionName) throws IntegrationException {
        Optional<ProjectView> projectView = getProjectByName(projectName);
        if (projectView.isPresent()) {
            Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView.get(), projectVersionName);

            if (projectVersionView.isPresent()) {
                return Optional.of(new ProjectVersionWrapper(projectView.get(), projectVersionView.get()));
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getNewestProjectVersion(ProjectView projectView) throws IntegrationException {
        List<ProjectVersionView> projectVersionViews = getAllProjectVersions(projectView);
        return projectVersionViews.stream().max(Comparator.comparing(ProjectVersionView::getCreatedAt));
    }

    public void updateProject(ProjectView projectView) throws IntegrationException {
        blackDuckApiClient.put(projectView);
    }

    public void updateProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        blackDuckApiClient.put(projectVersionView);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectSyncModel projectSyncModel) throws IntegrationException {
        return syncProjectAndVersion(projectSyncModel, false);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectSyncModel projectSyncModel, boolean performUpdate) throws IntegrationException {
        String projectName = projectSyncModel.getName();

        Optional<ProjectView> optionalProjectView = getProjectByName(projectName);
        if (!optionalProjectView.isPresent()) {
            logger.info(String.format("The %s project was not found, so it will be created - if a version was included, it will also be created.", projectName));
            ProjectRequest projectRequest = projectSyncModel.createProjectRequest();
            return createProject(projectRequest);
        }

        ProjectView projectView = optionalProjectView.get();
        if (performUpdate) {
            logger.info(String.format("The %s project was found and performUpdate=true, so it will be updated.", projectName));
            projectSyncModel.populateProjectView(projectView);
            blackDuckApiClient.put(projectView);
            projectView = blackDuckApiClient.getResponse(projectView.getHref(), ProjectView.class);
        }
        ProjectVersionView projectVersionView = null;

        if (projectSyncModel.shouldHandleProjectVersion()) {
            String projectVersionName = projectSyncModel.getVersionName();
            Optional<ProjectVersionView> optionalProjectVersionView = getProjectVersion(projectView, projectVersionName);
            if (optionalProjectVersionView.isPresent()) {
                projectVersionView = optionalProjectVersionView.get();
                if (performUpdate) {
                    logger.info(String.format("The %s version was found and performUpdate=true, so the version will be updated.", projectVersionName));
                    projectSyncModel.populateProjectVersionView(projectVersionView);
                    blackDuckApiClient.put(projectVersionView);
                    projectVersionView = blackDuckApiClient.getResponse(projectVersionView.getHref(), ProjectVersionView.class);
                }
            } else {
                logger.info(String.format("The %s version was not found, so it will be created under the %s project.", projectVersionName, projectName));
                ProjectVersionRequest projectVersionRequest = projectSyncModel.createProjectVersionRequest();
                projectVersionView = createProjectVersion(projectView, projectVersionRequest);
            }
        }

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

}
