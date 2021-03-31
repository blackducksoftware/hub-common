/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleComponentUsageValueSetType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionType;
import com.synopsys.integration.blackduck.api.enumeration.ReviewStatusType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsParametersView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleExpressionOperatorType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;

public class PolicyRuleExpressionSetBuilder {
    private final List<PolicyRuleExpressionExpressionsView> expressions = new ArrayList<>();

    public void addProjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ProjectView projectView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, projectView.getHref());
    }

    public void addComponentVersionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ComponentVersionView componentVersionView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, componentVersionView.getHref());
    }

    public void addComponentCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ComponentView componentView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, componentView.getHref());
    }

    public void addLicenseCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, LicenseView licenseView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_LICENSE, licenseView.getHref());
    }

    public void addReviewStatusCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ReviewStatusType reviewType) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.REVIEW_STATUS, reviewType);
    }

    public void addComponentReleaseDateCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Date date) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.RELEASE_DATE, RestConstants.formatDate(date));
    }

    public void addNewerVersionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.NEWER_VERSIONS_COUNT, count);
    }

    public void addHighSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.HIGH_SEVERITY_VULN_COUNT, count);
    }

    public void addMediumSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.MEDIUM_SEVERITY_VULN_COUNT, count);
    }

    public void addLowSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.LOW_SEVERITY_VULN_COUNT, count);
    }

    public void addProjectTierCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<Integer> tiers) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_TIER, tiers);
    }

    public void addPhaseCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<ProjectVersionPhaseType> projectVersionPhaseTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_PHASE, projectVersionPhaseTypes);
    }

    public void addDistributionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<ProjectVersionDistributionType> ProjectVersionDistributionTypes)
        throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_DISTRIBUTION, ProjectVersionDistributionTypes);
    }

    public void addComponentUsageCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<PolicyRuleComponentUsageValueSetType> componentUsageTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.COMPONENT_USAGE, componentUsageTypes);
    }

    public void addSingleObjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, Object object) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(1);
        values.add(object.toString());
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addMultiObjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, List<?> objectValues) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(objectValues.size());
        for (Object object : objectValues) {
            values.add(object.toString());
        }
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addSingleCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, HttpUrl httpUrl) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, policyRuleConditionType, httpUrl.string());
    }

    public void addSingleCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, String value) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(1);
        values.add(value);
        addMultiCondition(policyRuleConditionOperator, policyRuleConditionType, values);
    }

    public void addMultiCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, List<String> values) throws BlackDuckIntegrationException {
        PolicyRuleExpressionExpressionsParametersView expressionParameter = new PolicyRuleExpressionExpressionsParametersView();
        expressionParameter.setValues(values);
        PolicyRuleExpressionExpressionsView expression = new PolicyRuleExpressionExpressionsView();
        expression.setName(policyRuleConditionType.toString());
        expression.setOperation(policyRuleConditionOperator.toString());
        expression.setParameters(expressionParameter);
        expressions.add(expression);
    }

    public PolicyRuleExpressionView createPolicyRuleExpressionView() {
        return createPolicyRuleExpressionView(PolicyRuleExpressionOperatorType.AND);
    }

    public PolicyRuleExpressionView createPolicyRuleExpressionView(PolicyRuleExpressionOperatorType expressionOperatorType) {
        PolicyRuleExpressionView expressionSet = new PolicyRuleExpressionView();
        // TODO - setOperator was originally passed an enum
        expressionSet.setOperator(expressionOperatorType);
        expressionSet.setExpressions(expressions);
        return expressionSet;
    }

}
