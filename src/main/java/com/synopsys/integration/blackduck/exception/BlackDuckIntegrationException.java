/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckIntegrationException extends IntegrationException {
    private static final long serialVersionUID = 1L;

    public BlackDuckIntegrationException() {
        super();
    }

    public BlackDuckIntegrationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BlackDuckIntegrationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BlackDuckIntegrationException(final String message) {
        super(message);
    }

    public BlackDuckIntegrationException(final Throwable cause) {
        super(cause);
    }

}
