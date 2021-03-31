/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

public class ScanFailedException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public ScanFailedException() {
        super();
    }

    public ScanFailedException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ScanFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ScanFailedException(final String message) {
        super(message);
    }

    public ScanFailedException(final Throwable cause) {
        super(cause);
    }

}
