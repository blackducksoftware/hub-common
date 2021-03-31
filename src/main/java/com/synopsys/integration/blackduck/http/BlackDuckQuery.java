/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public class BlackDuckQuery {
    private final String q;

    public static Optional<BlackDuckQuery> createQuery(String parameter) {
        if (StringUtils.isNotBlank(parameter)) {
            return Optional.of(new BlackDuckQuery(parameter));
        }

        return Optional.empty();
    }

    public static Optional<BlackDuckQuery> createQuery(String prefix, String parameter) {
        if (StringUtils.isNotBlank(parameter)) {
            return Optional.of(new BlackDuckQuery(prefix + ":" + parameter));
        }

        return Optional.empty();
    }

    private BlackDuckQuery(String parameter) {
        q = parameter;
    }

    public String getParameter() {
        return q;
    }

}
