/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.AssignedUserGroupRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.AssignedUserRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.manual.temporary.view.AssignedUserView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class ProjectUsersService extends DataService {
    private final UserGroupService userGroupService;

    public ProjectUsersService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, UserGroupService userGroupService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.userGroupService = userGroupService;
    }

    public List<AssignedUserView> getAssignedUsersToProject(ProjectView project) throws IntegrationException {
        List<AssignedUserView> assignedUsers = blackDuckApiClient.getAllResponses(project, ProjectView.USERS_LINK_RESPONSE);
        return assignedUsers;
    }

    public List<UserView> getUsersForProject(ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.getName());
        List<AssignedUserView> assignedUsers = getAssignedUsersToProject(project);

        List<UserView> resolvedUserViews = new ArrayList<>();
        for (AssignedUserView assigned : assignedUsers) {
            HttpUrl userUrl = new HttpUrl(assigned.getUser());
            UserView userView = blackDuckApiClient.getResponse(userUrl, UserView.class);
            if (userView != null) {
                resolvedUserViews.add(userView);
            }
        }
        return resolvedUserViews;
    }

    public List<AssignedUserGroupView> getAssignedGroupsToProject(ProjectView project) throws IntegrationException {
        List<AssignedUserGroupView> assignedGroups = blackDuckApiClient.getAllResponses(project, ProjectView.USERGROUPS_LINK_RESPONSE);
        return assignedGroups;
    }

    public List<UserGroupView> getGroupsForProject(ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.getName());
        List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(project);

        List<UserGroupView> resolvedGroupViews = new ArrayList<>();
        for (AssignedUserGroupView assigned : assignedGroups) {
            HttpUrl groupUrl = new HttpUrl(assigned.getGroup());
            UserGroupView groupView = blackDuckApiClient.getResponse(groupUrl, UserGroupView.class);
            if (groupView != null) {
                resolvedGroupViews.add(groupView);
            }
        }
        return resolvedGroupViews;
    }

    /**
     * This will get all explicitly assigned users for a project, as well as all users who are assigned to groups that are explicitly assigned to a project.
     */
    public Set<UserView> getAllActiveUsersForProject(ProjectView projectView) throws IntegrationException {
        Set<UserView> users = new HashSet<>();

        List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(projectView);
        for (AssignedUserGroupView assignedUserGroupView : assignedGroups) {
            if (assignedUserGroupView.getActive()) {
                HttpUrl groupUrl = new HttpUrl(assignedUserGroupView.getGroup());
                UserGroupView userGroupView = blackDuckApiClient.getResponse(groupUrl, UserGroupView.class);
                if (userGroupView.getActive()) {
                    List<UserView> groupUsers = blackDuckApiClient.getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                    users.addAll(groupUsers);
                }
            }
        }

        List<AssignedUserView> assignedUsers = getAssignedUsersToProject(projectView);
        for (AssignedUserView assignedUser : assignedUsers) {
            HttpUrl userUrl = new HttpUrl(assignedUser.getUser());
            UserView userView = blackDuckApiClient.getResponse(userUrl, UserView.class);
            users.add(userView);
        }

        return users
                   .stream()
                   .filter(userView -> userView.getActive())
                   .collect(Collectors.toSet());
    }

    public void addGroupToProject(ProjectView projectView, String groupName) throws IntegrationException {
        Optional<UserGroupView> optionalUserGroupView = userGroupService.getGroupByName(groupName);
        UserGroupView userGroupView = optionalUserGroupView.orElseThrow(() -> new IntegrationException(String.format("The supplied group name (%s) does not exist.", groupName)));

        List<UserGroupView> currentGroups = getGroupsForProject(projectView);
        if (currentGroups.contains(userGroupView)) {
            logger.info(String.format("The supplied project (%s) already contained the group (%s).", projectView.getName(), groupName));
            return;
        }

        HttpUrl userGroupUrl = userGroupView.getHref();
        HttpUrl createUrl = projectView.getFirstLink(ProjectView.USERGROUPS_LINK);

        AssignedUserGroupRequest userGroupRequest = new AssignedUserGroupRequest();
        userGroupRequest.setGroup(userGroupUrl.string());
        blackDuckApiClient.post(createUrl, userGroupRequest);
    }

    public void addUserToProject(ProjectView projectView, String username) throws IntegrationException {
        List<UserView> allUsers = blackDuckApiClient.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        UserView userView = null;
        for (UserView user : allUsers) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                userView = user;
            }
        }
        if (null == userView) {
            throw new BlackDuckIntegrationException(String.format("The user (%s) does not exist.", username));
        }
        addUserToProject(projectView, userView);
    }

    public void addUserToProject(ProjectView projectView, UserView userView) throws IntegrationException {
        List<UserView> currentUsers = getUsersForProject(projectView);
        if (currentUsers.contains(userView)) {
            logger.info(String.format("The supplied project (%s) already contained the user (%s).", projectView.getName(), userView.getUserName()));
            return;
        }

        AssignedUserRequest assignedUserRequest = new AssignedUserRequest();
        HttpUrl userUrl = userView.getHref();
        assignedUserRequest.setUser(userUrl.string());

        HttpUrl addUserUrl = projectView.getFirstLink(ProjectView.USERS_LINK);
        blackDuckApiClient.post(addUserUrl, assignedUserRequest);
    }

}
