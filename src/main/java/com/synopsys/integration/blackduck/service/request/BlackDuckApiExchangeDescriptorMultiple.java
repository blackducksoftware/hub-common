/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

public class BlackDuckApiExchangeDescriptorMultiple<T extends BlackDuckResponse> extends BlackDuckApiExchangeDescriptor<T> {
    public BlackDuckApiExchangeDescriptorMultiple(BlackDuckRequestBuilder blackDuckRequestBuilder, Class<T> responseClass) {
        super(blackDuckRequestBuilder, responseClass);
    }

}
