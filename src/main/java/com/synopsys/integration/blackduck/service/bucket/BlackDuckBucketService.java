/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.bucket;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckBucketService extends DataService {
    private final ExecutorService executorService;

    public BlackDuckBucketService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, ExecutorService executorService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.executorService = executorService;
    }

    public <T extends BlackDuckResponse> Future<Optional<T>> addToTheBucket(BlackDuckBucket blackDuckBucket, String uri, Class<T> responseClass) {
        LinkSingleResponse<? extends BlackDuckResponse> linkSingleResponse = new LinkSingleResponse<>(uri, responseClass);
        BlackDuckBucketFillTask blackDuckBucketFillTask = new BlackDuckBucketFillTask(blackDuckApiClient, blackDuckBucket, linkSingleResponse);
        return executorService.submit(blackDuckBucketFillTask);
    }

}
