/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.api.enumeration;

public enum PolicyRuleConditionType {
    PROJECT_NAME(PolicyRuleCategoryType.PROJECT, "Project Name"),
    PROJECT_TAGS(PolicyRuleCategoryType.PROJECT, "Project Tags"),
    PROJECT_TIER(PolicyRuleCategoryType.PROJECT, "Project Tier"),
    VERSION_PHASE(PolicyRuleCategoryType.PROJECT, "Project Phase"),
    VERSION_DISTRIBUTION(PolicyRuleCategoryType.PROJECT, "Project Distribution Type"),
    SINGLE_VERSION(PolicyRuleCategoryType.COMPONENT, "Component"),
    COMPONENT_USAGE(PolicyRuleCategoryType.COMPONENT, "Component Usage"),
    REVIEW_STATUS(PolicyRuleCategoryType.COMPONENT, "Review Status"),
    LICENSE_FAMILY(PolicyRuleCategoryType.COMPONENT, "License Family"),
    SINGLE_LICENSE(PolicyRuleCategoryType.COMPONENT, "License"),
    RELEASE_DATE(PolicyRuleCategoryType.COMPONENT, "Component Release Date"),
    NEWER_VERSIONS_COUNT(PolicyRuleCategoryType.COMPONENT, "Newer Versions Count"),
    HIGH_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "High Severity Vulnerability Count"),
    MEDIUM_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "Medium Severity Vulnerability Count"),
    LOW_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "Low Severity Vulnerability Count"),
    UNKNOWN_RULE_CONDTION(null, "Unknown Rule Condition");

    private final PolicyRuleCategoryType policyRuleCategory;
    private final String displayValue;

    PolicyRuleConditionType(final PolicyRuleCategoryType policyRuleCategory, final String displayValue) {
        this.policyRuleCategory = policyRuleCategory;
        this.displayValue = displayValue;
    }

    public PolicyRuleCategoryType getPolicyRuleCategory() {
        return policyRuleCategory;
    }

    public String getDisplayValue() {
        return displayValue;
    }

}
