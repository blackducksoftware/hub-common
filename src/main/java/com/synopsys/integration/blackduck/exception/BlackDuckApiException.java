/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckApiException extends IntegrationException {
    private final IntegrationRestException originalIntegrationRestException;
    private final String blackDuckErrorCode;

    public BlackDuckApiException(IntegrationRestException originalIntegrationRestException, String blackDuckErrorMessage, String blackDuckErrorCode) {
        super(blackDuckErrorMessage);
        this.originalIntegrationRestException = originalIntegrationRestException;
        this.blackDuckErrorCode = blackDuckErrorCode;
    }

    public IntegrationRestException getOriginalIntegrationRestException() {
        return originalIntegrationRestException;
    }

    public String getBlackDuckErrorCode() {
        return blackDuckErrorCode;
    }

}
