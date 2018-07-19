/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.throwaway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.notification.NotificationResults;

public class NotificationDetailResultsOld extends NotificationResults<NotificationDetailResultOld> {
    private final List<NotificationDetailResultOld> resultList;

    public NotificationDetailResultsOld(final List<NotificationDetailResultOld> resultList, final Optional<Date> latestNotificationCreatedAtDate, final Optional<String> latestNotificationCreatedAtString) {
        super(latestNotificationCreatedAtDate.orElse(null), latestNotificationCreatedAtString.orElse(null));
        this.resultList = resultList;
    }

    public List<UriSingleResponse<? extends HubResponse>> getAllLinks() {
        final List<UriSingleResponse<? extends HubResponse>> uriResponses = new ArrayList<>();
        resultList.forEach(result -> {
            result.getNotificationContentDetails().forEach(contentDetail -> {
                uriResponses.addAll(contentDetail.getPresentLinks());
            });
        });

        return uriResponses;
    }

    @Override
    public List<NotificationDetailResultOld> getResults() {
        return resultList;
    }

}
