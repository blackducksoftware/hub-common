/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.bucket;

import java.util.Optional;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;

public class BlackDuckBucketFillTask<T extends BlackDuckResponse> implements Callable<Optional<T>> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final BlackDuckBucket blackDuckBucket;
    private final LinkSingleResponse<T> linkSingleResponse;

    public BlackDuckBucketFillTask(BlackDuckApiClient blackDuckApiClient, BlackDuckBucket blackDuckBucket, LinkSingleResponse<T> linkSingleResponse) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckBucket = blackDuckBucket;
        this.linkSingleResponse = linkSingleResponse;
    }

    @Override
    public Optional<T> call() {
        if (!blackDuckBucket.contains(linkSingleResponse.getLink())) {
            try {
                T blackDuckResponse = blackDuckApiClient.getResponse(linkSingleResponse);
                blackDuckBucket.addValid(linkSingleResponse.getLink(), blackDuckResponse);
                return Optional.of(blackDuckResponse);
            } catch (Exception e) {
                // it is up to the consumer of the bucket to log or handle any/all Exceptions
                blackDuckBucket.addError(linkSingleResponse.getLink(), e);
                return Optional.empty();
            }
        }
        return Optional.ofNullable(blackDuckBucket.get(linkSingleResponse.getLink(), linkSingleResponse.getResponseClass()));
    }

}
