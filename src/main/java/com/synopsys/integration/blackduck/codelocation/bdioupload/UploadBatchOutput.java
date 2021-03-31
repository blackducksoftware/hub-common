/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.List;

import com.synopsys.integration.blackduck.codelocation.CodeLocationBatchOutput;

public class UploadBatchOutput extends CodeLocationBatchOutput<UploadOutput> {
    public UploadBatchOutput(final List<UploadOutput> outputs) {
        super(outputs);
    }

}
