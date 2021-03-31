/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class SignatureScannerCodeLocationCreationRequest extends CodeLocationCreationRequest<ScanBatchOutput> {
    private final ScanBatchRunner scanBatchRunner;
    private final ScanBatch scanBatch;

    public SignatureScannerCodeLocationCreationRequest(final ScanBatchRunner scanBatchRunner, final ScanBatch scanBatch) {
        this.scanBatchRunner = scanBatchRunner;
        this.scanBatch = scanBatch;
    }

    @Override
    public ScanBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return scanBatchRunner.executeScans(scanBatch);
    }

}
