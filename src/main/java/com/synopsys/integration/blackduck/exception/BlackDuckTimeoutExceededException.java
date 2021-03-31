/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

public class BlackDuckTimeoutExceededException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public BlackDuckTimeoutExceededException() {
    }

    public BlackDuckTimeoutExceededException(final String message) {
        super(message);
    }

    public BlackDuckTimeoutExceededException(final Throwable cause) {
        super(cause);
    }

    public BlackDuckTimeoutExceededException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
