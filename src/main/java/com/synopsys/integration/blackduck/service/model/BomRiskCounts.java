/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.HIGH;
import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.LOW;
import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.MEDIUM;

import com.synopsys.integration.blackduck.api.generated.component.RiskProfileCountsView;

public class BomRiskCounts {
    private int high;
    private int medium;
    private int low;

    public void add(RiskProfileCountsView countsView) {
        int count = countsView.getCount().intValue();
        if (HIGH == countsView.getCountType()) {
            high += count;
        } else if (MEDIUM == countsView.getCountType()) {
            medium += count;
        } else if (LOW == countsView.getCountType()) {
            low += count;
        }
    }

    public void add(BomRiskCounts bomRiskCounts) {
        if (bomRiskCounts.getHigh() > 0) {
            high++;
        } else if (bomRiskCounts.getMedium() > 0) {
            medium++;
        } else if (bomRiskCounts.getLow() > 0) {
            low++;
        }
    }

    public int getHigh() {
        return high;
    }

    public int getMedium() {
        return medium;
    }

    public int getLow() {
        return low;
    }

}
