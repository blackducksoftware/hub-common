/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public abstract class CodeLocationCreationRequest<T extends CodeLocationBatchOutput> {
    public abstract T executeRequest() throws BlackDuckIntegrationException;

}
