/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.util.List;

import com.synopsys.integration.blackduck.codelocation.CodeLocationBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;

public class ScanBatchOutput extends CodeLocationBatchOutput<ScanCommandOutput> {
    public ScanBatchOutput(final List<ScanCommandOutput> scanCommandOutputs) {
        super(scanCommandOutputs);
    }

}
