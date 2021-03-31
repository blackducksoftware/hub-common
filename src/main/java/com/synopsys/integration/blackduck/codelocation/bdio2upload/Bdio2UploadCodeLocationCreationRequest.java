/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdio2upload;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class Bdio2UploadCodeLocationCreationRequest extends CodeLocationCreationRequest<UploadBatchOutput> {
    private final UploadBdio2BatchRunner uploadBdio2BatchRunner;
    private final UploadBatch uploadBatch;

    public Bdio2UploadCodeLocationCreationRequest(UploadBdio2BatchRunner uploadBdio2BatchRunner, UploadBatch uploadBatch) {
        this.uploadBdio2BatchRunner = uploadBdio2BatchRunner;
        this.uploadBatch = uploadBatch;
    }

    @Override
    public UploadBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return uploadBdio2BatchRunner.executeUploads(uploadBatch);
    }

}
