/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some requests to Black Duck can be filtered. There can be one more more filter keys each with one or more values.
 *
 * If multiple filters values are combined for the same filter key, these values will be OR'ed together. If multiple filter values are combined for different filter keys, these values will be AND'ed together.
 */
public class BlackDuckRequestFilter {
    private final Map<String, Set<String>> filterKeysToValues = new HashMap<>();

    public static BlackDuckRequestFilter createFilterWithMultipleValues(String key, List<String> values) {
        BlackDuckRequestFilter blackDuckRequestFilter = new BlackDuckRequestFilter();
        blackDuckRequestFilter.addFilter(key, values);
        return blackDuckRequestFilter;
    }

    public static BlackDuckRequestFilter createFilterWithSingleValue(String key, String value) {
        BlackDuckRequestFilter blackDuckRequestFilter = new BlackDuckRequestFilter();
        blackDuckRequestFilter.addFilter(key, value);
        return blackDuckRequestFilter;
    }

    public void addFilter(String key, String value) {
        filterKeysToValues.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }

    public void addFilter(String key, List<String> values) {
        filterKeysToValues.computeIfAbsent(key, k -> new HashSet<>()).addAll(values);
    }

    /**
     * This will return the filter key/value pairs as Black Duck expects them: [key1:value1,key1:value2,key2:value3] etc
     */
    public List<String> getFilterParameters() {
        List<String> parameters = new ArrayList<>();
        filterKeysToValues.forEach((filterKey, filterValues) -> {
            filterValues.forEach(filterValue -> {
                String parameterString = String.format("%s:%s", filterKey, filterValue);
                parameters.add(parameterString);
            });
        });

        return parameters;
    }

}
