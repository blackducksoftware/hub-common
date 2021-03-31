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

import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class IssueService extends DataService {
    public IssueService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<ProjectVersionIssuesView> getIssuesForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectVersionView, ProjectVersionView.ISSUES_LINK_RESPONSE);
    }

    public Optional<IssueView> getIssueByKey(ProjectVersionView projectVersionView, String issueKey) throws IntegrationException {
        Predicate<ProjectVersionIssuesView> issueKeyEquals = (issue -> issue.getIssueId().equals(issueKey));

        List<ProjectVersionIssuesView> bomComponentIssues = blackDuckApiClient.getSomeMatchingResponses(projectVersionView, ProjectVersionView.ISSUES_LINK_RESPONSE, issueKeyEquals, 1);
        Optional<ProjectVersionIssuesView> projectVersionIssuesViewOptional = bomComponentIssues.stream().findAny();

        if (projectVersionIssuesViewOptional.isPresent()) {
            return Optional.ofNullable(getIssueView(projectVersionIssuesViewOptional.get()));
        }

        return Optional.empty();
    }

    public IssueView getIssueView(ProjectVersionIssuesView projectVersionIssuesView) throws IntegrationException {
        HttpUrl issueUrl = projectVersionIssuesView.getHref();
        return blackDuckApiClient.getResponse(issueUrl, IssueView.class);
    }

    public void createIssueForComponent(ProjectVersionComponentView projectVersionComponentView, IssueRequest issueRequest) throws IntegrationException {
        HttpUrl createIssueUrl = projectVersionComponentView.getFirstLink(ProjectVersionComponentView.COMPONENT_ISSUES_LINK);
        blackDuckApiClient.post(createIssueUrl, issueRequest);
    }

    public void updateIssue(IssueView issueView) throws IntegrationException {
        blackDuckApiClient.put(issueView);
    }

}
