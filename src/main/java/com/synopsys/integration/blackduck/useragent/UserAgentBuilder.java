/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.useragent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class UserAgentBuilder {
    private List<UserAgentItem> userAgentItems = new ArrayList<>();

    public void addUserAgent(UserAgentItem userAgentItem) {
        if (null == userAgentItem || null == userAgentItem.getProduct() || StringUtils.isBlank(userAgentItem.getProduct().getName())) {
            return;
        }
        userAgentItems.add(userAgentItem);
    }

    public String createFullUserAgentString() {
        return userAgentItems
            .stream()
            .map(UserAgentItem::createUserAgentString)
            .collect(Collectors.joining(" "));
    }

}
