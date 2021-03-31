/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;

public class CodeLocationCreationData<T extends CodeLocationBatchOutput> {
    private final NotificationTaskRange notificationTaskRange;
    private final T output;

    public CodeLocationCreationData(NotificationTaskRange notificationTaskRange, T output) {
        this.notificationTaskRange = notificationTaskRange;
        this.output = output;
    }

    public NotificationTaskRange getNotificationTaskRange() {
        return notificationTaskRange;
    }

    public T getOutput() {
        return output;
    }

}
