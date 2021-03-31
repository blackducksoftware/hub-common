/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

public class CookieHeaderParser {
    public static final String SET_COOKIE = "SET-COOKIE";
    public static final String AUTHORIZATION_BEARER_PREFIX = "AUTHORIZATION_BEARER=";
    public static final String HEADER_VALUE_SEPARATOR = ";";

    public Optional<String> parseBearerToken(Header[] allHeaders) {
        return Arrays
                   .stream(allHeaders)
                   // https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
                   // header names are case insensitive
                   .filter(header -> SET_COOKIE.equalsIgnoreCase(header.getName()))
                   .filter(header -> header.getValue().contains(AUTHORIZATION_BEARER_PREFIX))
                   .findFirst()
                   .map(header -> getToken(header.getValue()));
    }

    private String getToken(String headerValue) {
        if (headerValue.contains(HEADER_VALUE_SEPARATOR)) {
            return StringUtils.substringBetween(headerValue, AUTHORIZATION_BEARER_PREFIX, HEADER_VALUE_SEPARATOR);
        } else {
            return StringUtils.substringAfter(headerValue, AUTHORIZATION_BEARER_PREFIX);
        }
    }

}
