/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class BinaryScanOutput extends CodeLocationOutput {
    private final String response;
    private final String statusMessage;
    private final int statusCode;
    private final String contentString;

    public static BinaryScanOutput FAILURE(NameVersion projectAndVersion, String codeLocationName, String errorMessage, Exception exception) {
        return new BinaryScanOutput(Result.FAILURE, projectAndVersion, codeLocationName, errorMessage, exception, null, null, -1, null);
    }

    public static BinaryScanOutput FROM_INTEGRATION_REST_EXCEPTION(NameVersion projectAndVersion, String codeLocationName, IntegrationRestException e) {
        return new BinaryScanOutput(Result.FAILURE, projectAndVersion, codeLocationName, e.getMessage(), e, e.getHttpResponseContent(), e.getHttpStatusMessage(), e.getHttpStatusCode(), null);
    }

    public static BinaryScanOutput FROM_RESPONSE(NameVersion projectAndVersion, String codeLocationName, Response response) {
        String responseString = response.toString();
        String statusMessage = response.getStatusMessage();
        int statusCode = response.getStatusCode();
        String contentString = null;
        IntegrationException contentStringException = null;
        try {
            contentString = response.getContentString();
        } catch (IntegrationException e) {
            contentStringException = e;
        }

        Result result = Result.SUCCESS;
        String errorMessage = null;
        if (!response.isStatusCodeSuccess()) {
            result = Result.FAILURE;
            errorMessage = "Binary scan upload failure - status code: " + response.getStatusCode() + ", " + response.getStatusMessage();
        } else if (null != contentStringException) {
            result = Result.FAILURE;
            errorMessage = contentStringException.getMessage();
        }

        return new BinaryScanOutput(result, projectAndVersion, codeLocationName, errorMessage, contentStringException, responseString, statusMessage, statusCode, contentString);
    }

    private BinaryScanOutput(Result result, NameVersion projectAndVersion, String codeLocationName, String errorMessage, Exception exception, String response, String statusMessage, int statusCode, String contentString) {
        super(result, projectAndVersion, codeLocationName, 1, errorMessage, exception);
        this.response = response;
        this.statusMessage = statusMessage;
        this.statusCode = statusCode;
        this.contentString = contentString;
    }

    public String getResponse() {
        return response;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentString() {
        return contentString;
    }

}
