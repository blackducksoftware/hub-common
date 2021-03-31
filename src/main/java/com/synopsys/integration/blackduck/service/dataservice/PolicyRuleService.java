/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class PolicyRuleService extends DataService {
    public PolicyRuleService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<PolicyRuleView> getAllPolicyRules() throws IntegrationException {
        return blackDuckApiClient.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
    }

    public Optional<PolicyRuleView> getPolicyRuleViewByName(String policyRuleName) throws IntegrationException {
        List<PolicyRuleView> allPolicyRules = blackDuckApiClient.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
        for (PolicyRuleView policyRule : allPolicyRules) {
            if (policyRuleName.equals(policyRule.getName())) {
                return Optional.of(policyRule);
            }
        }
        return Optional.empty();
    }

    public HttpUrl createPolicyRule(PolicyRuleView policyRuleView) throws IntegrationException {
        return blackDuckApiClient.post(ApiDiscovery.POLICY_RULES_LINK, policyRuleView);
    }

    /**
     * This will create a policy rule that will be violated by the existence of a matching external id in the project's BOM.
     */
    public HttpUrl createPolicyRuleForExternalId(ComponentService componentService, ExternalId externalId, String policyName) throws IntegrationException {
        Optional<ComponentsView> componentSearchResult = componentService.getSingleOrEmptyResult(externalId);
        if (!componentSearchResult.isPresent()) {
            throw new BlackDuckIntegrationException(String.format("The external id (%s) provided could not be found, so no policy can be created for it.", externalId.createExternalId()));
        }

        Optional<ComponentVersionView> componentVersionView = componentService.getComponentVersionView(componentSearchResult.get());
        if (!componentVersionView.isPresent()) {
            throw new BlackDuckIntegrationException(String.format("A component version could not be found for the provided external id (%s), so no policy can be created for it.", externalId.createExternalId()));
        }

        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView.get());
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(policyName);
        policyRuleView.setEnabled(true);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);

        return createPolicyRule(policyRuleView);
    }

}
