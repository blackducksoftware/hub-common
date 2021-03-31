/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.blackduck.api.generated.component.RiskProfileCountsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;

public class RiskProfileCounts {
    private final Map<RiskPriorityType, BigDecimal> countsMap;

    public RiskProfileCounts(RiskProfileView view) {
        countsMap = new HashMap<>();
        for (RiskPriorityType value : RiskPriorityType.values()) {
            countsMap.put(value, new BigDecimal(0));
        }
        if (view != null) {
            for (RiskProfileCountsView count : view.getCounts()) {
                countsMap.put(count.getCountType(), count.getCount());
            }
        }
    }

    public BigDecimal getCount(RiskPriorityType level) {
        return countsMap.get(level);
    }

}
