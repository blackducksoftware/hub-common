/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.List;

import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;

public class ComponentVersionVulnerabilities {
    private final ComponentVersionView componentVersionView;
    private final List<VulnerabilityView> vulnerabilities;

    public ComponentVersionVulnerabilities(ComponentVersionView componentVersionView, List<VulnerabilityView> vulnerabilities) {
        this.componentVersionView = componentVersionView;
        this.vulnerabilities = vulnerabilities;
    }

    public ComponentVersionView getComponentVersionView() {
        return componentVersionView;
    }

    public List<VulnerabilityView> getVulnerabilities() {
        return vulnerabilities;
    }

}
