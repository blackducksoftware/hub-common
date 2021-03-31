/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.RoleAssignmentRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class UserRoleService extends DataService {
    public UserRoleService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public void addServerRoleToUser(RoleView roleView, UserView userView) throws IntegrationException {
        RoleAssignmentRequest roleAssignmentRequest = initializeRequest(roleView);
        roleAssignmentRequest.setScope(RoleService.SERVER_SCOPE);

        createUserRole(userView, roleAssignmentRequest);
    }

    public void addProjectRoleToUser(RoleView roleView, ProjectView projectView, UserView userView) throws IntegrationException {
        RoleAssignmentRequest roleAssignmentRequest = initializeRequest(roleView);
        roleAssignmentRequest.setScope(projectView.getHref().string());

        createUserRole(userView, roleAssignmentRequest);
    }

    @NotNull
    private RoleAssignmentRequest initializeRequest(RoleView roleView) {
        RoleAssignmentRequest roleAssignmentRequest = new RoleAssignmentRequest();
        roleAssignmentRequest.setRole(roleView.getHref().string());
        return roleAssignmentRequest;
    }

    private void createUserRole(UserView userView, RoleAssignmentRequest roleAssignmentRequest) throws IntegrationException {
        HttpUrl userRoleUrl = userView.getFirstLink(UserView.ROLES_LINK);
        blackDuckApiClient.post(userRoleUrl, roleAssignmentRequest);
    }

}
