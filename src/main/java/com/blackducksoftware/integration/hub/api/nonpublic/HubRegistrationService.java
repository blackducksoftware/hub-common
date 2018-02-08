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
package com.blackducksoftware.integration.hub.api.nonpublic;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_REGISTRATIONS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.io.IOException;
import java.util.Arrays;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.google.gson.JsonObject;

public class HubRegistrationService extends HubService {
    public HubRegistrationService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String getRegistrationId() throws IntegrationException, IOException {
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_REGISTRATIONS));
        final Request request = new Request(uri);

        try (Response response = getRestConnection().executeRequest(request)) {
            final String jsonResponse = response.getContentString();
            final JsonObject jsonObject = getJsonParser().parse(jsonResponse).getAsJsonObject();
            final String registrationId = jsonObject.get("registrationId").getAsString();
            return registrationId;
        }
    }

}