/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

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

public class BinaryScanUploadService extends DataService {
    private final BinaryScanBatchRunner binaryScanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public BinaryScanUploadService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger, BinaryScanBatchRunner binaryScanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.binaryScanBatchRunner = binaryScanBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public BinaryScanCodeLocationCreationRequest createUploadRequest(BinaryScanBatch uploadBatch) {
        return new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScanCodeLocationCreationRequest uploadRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(uploadRequest);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScanBatch uploadBatch) throws IntegrationException {
        BinaryScanCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBinaryScan(uploadRequest);
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScan(BinaryScan binaryScan) throws IntegrationException {
        BinaryScanBatch uploadBatch = new BinaryScanBatch();
        uploadBatch.addBinaryScan(binaryScan);
        BinaryScanCodeLocationCreationRequest uploadRequest = new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);

        return uploadBinaryScan(uploadRequest);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScanCodeLocationCreationRequest uploadRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(uploadRequest, timeoutInSeconds);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScanBatch uploadBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BinaryScanCodeLocationCreationRequest uploadRequest = createUploadRequest(uploadBatch);
        return uploadBinaryScanAndWait(uploadRequest, timeoutInSeconds);
    }

    public BinaryScanBatchOutput uploadBinaryScanAndWait(BinaryScan binaryScan, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        BinaryScanBatch uploadBatch = new BinaryScanBatch();
        uploadBatch.addBinaryScan(binaryScan);
        BinaryScanCodeLocationCreationRequest uploadRequest = new BinaryScanCodeLocationCreationRequest(binaryScanBatchRunner, uploadBatch);

        return uploadBinaryScanAndWait(uploadRequest, timeoutInSeconds);
    }

    public void waitForBinaryScanUpload(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
        throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
