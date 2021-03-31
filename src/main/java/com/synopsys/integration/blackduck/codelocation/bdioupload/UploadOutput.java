/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.Optional;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.util.NameVersion;

public class UploadOutput extends CodeLocationOutput {
    private final String response;

    public static UploadOutput SUCCESS(final NameVersion projectAndVersion, final String codeLocationName, final String response) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.SUCCESS, response, null, null);
    }

    public static UploadOutput FAILURE(final NameVersion projectAndVersion, final String codeLocationName, final String errorMessage, final Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, null, errorMessage, exception);
    }

    public static UploadOutput FAILURE(final NameVersion projectAndVersion, final String codeLocationName, final String response, final String errorMessage, final Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, response, errorMessage, exception);
    }

    private UploadOutput(final NameVersion projectAndVersion, final String codeLocationName, final Result result, final String response, final String errorMessage, final Exception exception) {
        super(result, projectAndVersion, codeLocationName, 1, errorMessage, exception);
        this.response = response;
    }

    public Optional<String> getResponse() {
        return Optional.ofNullable(response);
    }

}
