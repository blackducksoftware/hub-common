/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class BdioUploadService extends DataService {
    private final UploadBatchRunner uploadBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BdioUploadService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, UploadBatchRunner uploadBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.uploadBatchRunner = uploadBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public BdioUploadCodeLocationCreationRequest createUploadRequest(UploadBatch uploadBatch) {
        return new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(BdioUploadCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadBatch uploadBatch) throws IntegrationException {
        BdioUploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdio(uploadRequest);
    }

    public CodeLocationCreationData<UploadBatchOutput> uploadBdio(UploadTarget uploadTarget) throws IntegrationException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        return uploadBdio(uploadRequest);
    }

    public UploadBatchOutput uploadBdioAndWait(BdioUploadCodeLocationCreationRequest uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BdioUploadCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public UploadBatchOutput uploadBdioAndWait(UploadTarget uploadTarget, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(uploadTarget);
        BdioUploadCodeLocationCreationRequest uploadRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        return uploadBdioAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
