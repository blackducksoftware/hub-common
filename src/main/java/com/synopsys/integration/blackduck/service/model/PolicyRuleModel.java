/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.Collections;
import java.util.List;

import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;

public class PolicyRuleModel {
    private final PolicyRuleView rule;

    public PolicyRuleModel(PolicyRuleView rule) {
        this.rule = rule;
    }

    public boolean hasExpressions() {
        return rule != null && rule.getExpression() != null && rule.getExpression().getExpressions() != null && !rule.getExpression().getExpressions().isEmpty();
    }

    public List<PolicyRuleExpressionExpressionsView> getExpressionList() {
        if (hasExpressions()) {
            return rule.getExpression().getExpressions();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasOnlyProjectLevelConditions() {
        boolean hasNonProjectLevelCondition = false;

        for (PolicyRuleExpressionExpressionsView expression : getExpressionList()) {
            PolicyRuleConditionType condition = PolicyRuleConditionType.valueOf(expression.getName());
            if (PolicyRuleConditionType.UNKNOWN_RULE_CONDTION == condition) {
                continue;
            }
            if (PolicyRuleCategoryType.COMPONENT == condition.getPolicyRuleCategory()) {
                hasNonProjectLevelCondition = true;
            }
        }
        return !hasNonProjectLevelCondition;
    }

}
