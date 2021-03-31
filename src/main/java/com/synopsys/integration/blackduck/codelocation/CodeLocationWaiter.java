/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.CodeLocationWaitJobTask;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.wait.WaitJob;

public class CodeLocationWaiter {
    private final IntLogger logger;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ProjectService projectService;
    private final NotificationService notificationService;

    public CodeLocationWaiter(IntLogger logger, BlackDuckApiClient blackDuckApiClient, ProjectService projectService, NotificationService notificationService) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.projectService = projectService;
        this.notificationService = notificationService;
    }

    public CodeLocationWaitResult checkCodeLocationsAddedToBom(UserView userView, NotificationTaskRange notificationTaskRange, NameVersion projectAndVersion, Set<String> codeLocationNames, int expectedNotificationCount,
        long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        CodeLocationWaitJobTask codeLocationWaitJobTask = new CodeLocationWaitJobTask(logger, blackDuckApiClient, projectService, notificationService, userView, notificationTaskRange, projectAndVersion, codeLocationNames,
            expectedNotificationCount);

        // if a timeout of 0 is provided and the timeout check is done too quickly, w/o a do/while, no check will be performed
        // regardless of the timeout provided, we always want to check at least once
        boolean allCompleted = codeLocationWaitJobTask.isComplete();

        // waitInterval needs to be less than the timeout
        if (waitIntervalInSeconds > timeoutInSeconds) {
            waitIntervalInSeconds = (int) timeoutInSeconds;
        }

        if (!allCompleted) {
            WaitJob waitJob = WaitJob.create(logger, timeoutInSeconds, notificationTaskRange.getTaskStartTime(), waitIntervalInSeconds, codeLocationWaitJobTask);
            allCompleted = waitJob.waitFor();
        }

        if (!allCompleted) {
            return CodeLocationWaitResult
                       .PARTIAL(codeLocationWaitJobTask.getFoundCodeLocationNames(), String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds));
        } else {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(codeLocationWaitJobTask.getFoundCodeLocationNames());
        }
    }

}
