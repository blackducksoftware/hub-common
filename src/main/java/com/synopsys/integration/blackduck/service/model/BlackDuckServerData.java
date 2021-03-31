/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.Optional;

import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckServerData {
    private final HttpUrl url;
    private final String version;
    private final String registrationKey;

    public BlackDuckServerData(HttpUrl url, String version, String registrationKey) {
        this.url = url;
        this.version = version;
        this.registrationKey = registrationKey;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public Optional<String> getRegistrationKey() {
        return Optional.ofNullable(registrationKey);
    }

}
