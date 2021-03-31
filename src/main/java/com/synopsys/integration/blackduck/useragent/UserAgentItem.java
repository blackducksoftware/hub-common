/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.useragent;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.NameVersion;

public class UserAgentItem {
    public static final String UNKNOWN = "unknown";

    private final NameVersion product;
    private final String comments;

    public UserAgentItem(final NameVersion product, final String comments) {
        this.product = product;
        this.comments = comments;
    }

    public UserAgentItem(final NameVersion product) {
        this(product, null);
    }

    public NameVersion getProduct() {
        return product;
    }

    public Optional<String> getComments() {
        return Optional.ofNullable(comments);
    }

    public String createUserAgentString() {
        StringBuilder userAgent = new StringBuilder();
        userAgent.append(strip(clean(product.getName())));
        userAgent.append("/");
        userAgent.append(strip(clean(product.getVersion())));

        String cleaned = clean(comments);
        if (!UNKNOWN.equals(cleaned)) {
            userAgent.append(" (");
            userAgent.append(cleaned);
            userAgent.append(")");
        }

        return userAgent.toString();
    }

    private String strip(String original) {
        return original.replace(" ", "");
    }

    private String clean(String original) {
        String value = StringUtils.trimToEmpty(original);
        if (!value.isEmpty()) {
            return value;
        } else {
            return UNKNOWN;
        }
    }

}
