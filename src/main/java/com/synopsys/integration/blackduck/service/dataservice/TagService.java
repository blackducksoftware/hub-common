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

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.TagView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class TagService extends DataService {
    public TagService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<TagView> getAllTags(ProjectView projectView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectView, ProjectView.TAGS_LINK_RESPONSE);
    }

    public Optional<TagView> findMatchingTag(ProjectView projectView, String tagName) throws IntegrationException {
        return getAllTags(projectView)
                   .stream()
                   .filter(tagView -> tagView.getName().equals(tagName))
                   .findFirst();
    }

    public void updateTag(TagView tag) throws IntegrationException {
        blackDuckApiClient.put(tag);
    }

    public TagView createTag(ProjectView projectView, TagView tag) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.TAGS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a tag.", ProjectView.TAGS_LINK));
        }
        HttpUrl tagsLink = projectView.getFirstLink(ProjectView.TAGS_LINK);
        HttpUrl tagLink = blackDuckApiClient.post(tagsLink, tag);
        return blackDuckApiClient.getResponse(tagLink, TagView.class);
    }

}
