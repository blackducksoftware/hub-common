/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.Date;

public class NotificationTaskRange {
    private final long taskStartTime;
    private final Date startDate;
    private final Date endDate;

    public NotificationTaskRange(final long taskStartTime, final Date startDate, final Date endDate) {
        this.taskStartTime = taskStartTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

}
