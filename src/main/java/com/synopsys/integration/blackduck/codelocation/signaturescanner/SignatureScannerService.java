/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaitResult;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class SignatureScannerService extends DataService {
    private final ScanBatchRunner scanBatchRunner;
    private final CodeLocationCreationService codeLocationCreationService;

    public SignatureScannerService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger,
        ScanBatchRunner scanBatchRunner, CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
        this.scanBatchRunner = scanBatchRunner;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public SignatureScannerCodeLocationCreationRequest createScanRequest(ScanBatch scanBatch) {
        return new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(SignatureScannerCodeLocationCreationRequest scanRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(scanRequest);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(ScanBatch scanBatch) throws IntegrationException {
        SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScan(scanRequest);
    }

    public ScanBatchOutput performSignatureScanAndWait(SignatureScannerCodeLocationCreationRequest scanRequest, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(scanRequest, timeoutInSeconds);
    }

    public ScanBatchOutput performSignatureScanAndWait(ScanBatch scanBatch, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchRunner, scanBatch);

        return performSignatureScanAndWait(scanRequest, timeoutInSeconds);
    }

    public CodeLocationWaitResult waitForSignatureScan(NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames,
        int expectedNotificationCount, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, codeLocationNames, expectedNotificationCount, timeoutInSeconds);
    }

}
