/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.NameVersion;

public abstract class CodeLocationBatchOutput<T extends CodeLocationOutput> implements Iterable<T> {
    private final Map<String, Integer> successfulCodeLocationNamesToExpectedNotificationCounts;
    private final List<T> outputs = new ArrayList<>();

    public CodeLocationBatchOutput(List<T> outputs) {
        successfulCodeLocationNamesToExpectedNotificationCounts =
            outputs
                .stream()
                .peek(this.outputs::add)
                .filter(output -> Result.SUCCESS == output.getResult())
                .filter(output -> StringUtils.isNotBlank(output.getCodeLocationName()))
                .collect(Collectors.toMap(CodeLocationOutput::getCodeLocationName, CodeLocationOutput::getExpectedNotificationCount));
    }

    public List<T> getOutputs() {
        return outputs;
    }

    public NameVersion getProjectAndVersion() {
        return outputs
                   .stream()
                   .map(CodeLocationOutput::getProjectAndVersion)
                   .findFirst()
                   .get();
    }

    public Set<String> getSuccessfulCodeLocationNames() {
        return successfulCodeLocationNamesToExpectedNotificationCounts.keySet();
    }

    public int getExpectedNotificationCount() {
        return successfulCodeLocationNamesToExpectedNotificationCounts
                   .values()
                   .stream()
                   .mapToInt(Integer::intValue)
                   .sum();
    }

    public boolean hasAnyFailures() {
        return outputs
                   .stream()
                   .map(CodeLocationOutput::getResult)
                   .anyMatch(Result.FAILURE::equals);
    }

    @Override
    public Iterator<T> iterator() {
        return outputs.iterator();
    }

}
