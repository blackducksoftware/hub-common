/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

public class FailureConditionException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public FailureConditionException() {
        super();
    }

    public FailureConditionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FailureConditionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FailureConditionException(final String message) {
        super(message);
    }

    public FailureConditionException(final Throwable cause) {
        super(cause);
    }

}
