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

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserRequest;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class UserService extends DataService {
    public UserService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public UserRequest createUserRequest(String username, String password, String firstName, String lastName) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName(username);
        userRequest.setPassword(password);
        userRequest.setFirstName(firstName);
        userRequest.setLastName(lastName);
        userRequest.setActive(true);
        return userRequest;
    }

    public UserView createUser(UserRequest userRequest) throws IntegrationException {
        HttpUrl userUrl = blackDuckApiClient.post(ApiDiscovery.USERS_LINK, userRequest);
        return blackDuckApiClient.getResponse(userUrl, UserView.class);
    }

    public BlackDuckPageResponse<UserView> findUsersByEmail(String emailSearchTerm, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        HttpUrl usersUrl = blackDuckApiClient.getUrl(ApiDiscovery.USERS_LINK);
        Optional<BlackDuckQuery> usernameQuery = BlackDuckQuery.createQuery("email", emailSearchTerm);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(usersUrl, usernameQuery);
        return blackDuckApiClient.getPageResponse(blackDuckRequestBuilder, UserView.class, blackDuckPageDefinition);
    }

    public Optional<UserView> findUserByUsername(String username) throws IntegrationException {
        HttpUrl usersUrl = blackDuckApiClient.getUrl(ApiDiscovery.USERS_LINK);
        Optional<BlackDuckQuery> usernameQuery = BlackDuckQuery.createQuery("userName", username);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(usersUrl, usernameQuery);
        List<UserView> foundUsers = blackDuckApiClient.getSomeResponses(blackDuckRequestBuilder, UserView.class, 1);
        return foundUsers.stream().findFirst();
    }

    public List<UserView> getAllUsers() throws IntegrationException {
        return blackDuckApiClient.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
    }

    public BlackDuckPageResponse<UserView> getPageOfUsers(BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        return blackDuckApiClient.getPageResponse(ApiDiscovery.USERS_LINK_RESPONSE, blackDuckPageDefinition);
    }

}
