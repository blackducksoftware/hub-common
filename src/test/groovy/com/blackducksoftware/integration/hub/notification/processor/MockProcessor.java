/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.notification.processor;

import java.util.Collection;
import java.util.LinkedList;

import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.throwaway.MapProcessorCache;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.NotificationProcessor;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.throwaway.VulnerabilityContentItem;

public class MockProcessor extends NotificationProcessor<Collection<NotificationEvent>> {

    public MockProcessor(final MetaHandler metaService) {
        final MapProcessorCache cache = new MapProcessorCache();
        getCacheList().add(cache);
        getProcessorMap().put(PolicyViolationContentItem.class, new MockEventProcessor(cache, metaService));
        getProcessorMap().put(PolicyViolationClearedContentItem.class, new MockEventProcessor(cache, metaService));
        getProcessorMap().put(PolicyOverrideContentItem.class, new MockEventProcessor(cache, metaService));
        getProcessorMap().put(VulnerabilityContentItem.class,
                new MockEventProcessor(cache, metaService));
    }

    @Override
    public Collection<NotificationEvent> processEvents(final Collection<NotificationEvent> eventCollection) {
        final Collection<NotificationEvent> dataList = new LinkedList<>();
        for (final NotificationEvent entry : eventCollection) {
            dataList.add(entry);
        }
        return dataList;
    }
}
