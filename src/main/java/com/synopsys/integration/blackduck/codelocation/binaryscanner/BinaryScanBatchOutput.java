/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.util.List;

import com.synopsys.integration.blackduck.codelocation.CodeLocationBatchOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.log.IntLogger;

public class BinaryScanBatchOutput extends CodeLocationBatchOutput<BinaryScanOutput> {
    public BinaryScanBatchOutput(List<BinaryScanOutput> outputs) {
        super(outputs);
    }

    public void throwExceptionForError(IntLogger logger) throws BlackDuckIntegrationException {
        for (BinaryScanOutput binaryScanOutput : this) {
            if (binaryScanOutput.getResult() == Result.FAILURE) {
                String uploadErrorMessage = "Error when uploading binary scan: %s" + binaryScanOutput.getErrorMessage().orElse(binaryScanOutput.getStatusMessage());
                logger.error(uploadErrorMessage);
                throw new BlackDuckIntegrationException(uploadErrorMessage);
            }
        }
    }

}
