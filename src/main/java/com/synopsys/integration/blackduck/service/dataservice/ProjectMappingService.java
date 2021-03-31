/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectMappingView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class ProjectMappingService extends DataService {
    public ProjectMappingService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public void populateApplicationId(ProjectView projectView, String applicationId) throws IntegrationException {
        List<ProjectMappingView> projectMappings = blackDuckApiClient.getAllResponses(projectView, ProjectView.PROJECT_MAPPINGS_LINK_RESPONSE);
        boolean canCreate = projectMappings.isEmpty();
        if (canCreate) {
            if (!projectView.hasLink(ProjectView.PROJECT_MAPPINGS_LINK)) {
                throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a project mapping.", ProjectView.PROJECT_MAPPINGS_LINK));
            }
            HttpUrl projectMappingsLink = projectView.getFirstLink(ProjectView.PROJECT_MAPPINGS_LINK);
            ProjectMappingView projectMappingView = new ProjectMappingView();
            projectMappingView.setApplicationId(applicationId);
            blackDuckApiClient.post(projectMappingsLink, projectMappingView);
        } else {
            // Currently there exists only one project-mapping which is the project's Application ID.
            // Eventually, this method would need to take in a namespace on which we will need to filter.
            ProjectMappingView projectMappingView = projectMappings.get(0);
            projectMappingView.setApplicationId(applicationId);
            blackDuckApiClient.put(projectMappingView);
        }
    }

    public List<ProjectMappingView> getProjectMappings(ProjectView projectView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectView, ProjectView.PROJECT_MAPPINGS_LINK_RESPONSE);
    }

}
