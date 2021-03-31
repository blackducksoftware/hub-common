/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.bucket;

import java.util.Optional;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.util.Stringable;

public class BlackDuckBucketItem<T extends BlackDuckResponse> extends Stringable {
    private final String uri;
    private final Optional<T> blackDuckResponse;
    private final Optional<Exception> e;

    public BlackDuckBucketItem(final String uri, final T blackDuckResponse) {
        this.uri = uri;
        this.blackDuckResponse = Optional.of(blackDuckResponse);
        e = Optional.empty();
    }

    public BlackDuckBucketItem(final String uri, final Exception e) {
        this.uri = uri;
        blackDuckResponse = Optional.empty();
        this.e = Optional.of(e);
    }

    public boolean hasException() {
        return e.isPresent();
    }

    public boolean hasValidResponse() {
        return !e.isPresent();
    }

    public String getUri() {
        return uri;
    }

    public Optional<T> getBlackDuckResponse() {
        return blackDuckResponse;
    }

    public Optional<Exception> getE() {
        return e;
    }

}
