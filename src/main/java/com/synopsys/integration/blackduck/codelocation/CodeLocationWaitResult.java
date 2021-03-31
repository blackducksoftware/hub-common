/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.Optional;
import java.util.Set;

public class CodeLocationWaitResult {
    public enum Status {
        COMPLETE,
        PARTIAL
    }

    private final Status status;
    private final Set<String> codeLocationNames;
    private final String errorMessage;

    public static CodeLocationWaitResult COMPLETE(Set<String> codeLocationNames) {
        return new CodeLocationWaitResult(Status.COMPLETE, codeLocationNames, null);
    }

    public static CodeLocationWaitResult PARTIAL(Set<String> codeLocationNames, String errorMessage) {
        return new CodeLocationWaitResult(Status.PARTIAL, codeLocationNames, errorMessage);
    }

    public CodeLocationWaitResult(Status status, Set<String> codeLocationNames, String errorMessage) {
        this.status = status;
        this.codeLocationNames = codeLocationNames;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public Set<String> getCodeLocationNames() {
        return codeLocationNames;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

}
