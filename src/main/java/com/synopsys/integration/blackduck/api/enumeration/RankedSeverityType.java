/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.api.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

public enum RankedSeverityType {
    UNSPECIFIED(PolicyRuleSeverityType.UNSPECIFIED),
    TRIVIAL(PolicyRuleSeverityType.TRIVIAL),
    MINOR(PolicyRuleSeverityType.MINOR),
    MAJOR(PolicyRuleSeverityType.MAJOR),
    CRITICAL(PolicyRuleSeverityType.CRITICAL),
    BLOCKER(PolicyRuleSeverityType.BLOCKER);

    private final PolicyRuleSeverityType policyRuleSeverityType;

    private RankedSeverityType(PolicyRuleSeverityType unrankedSeverityType) {
        this.policyRuleSeverityType = unrankedSeverityType;
    }

    public static List<PolicyRuleSeverityType> getRankedValues() {
        return Arrays
                   .stream(values())
                   .map(t -> t.policyRuleSeverityType)
                   .collect(Collectors.toList());
    }
}
