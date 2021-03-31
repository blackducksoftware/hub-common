/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.NameVersion;

public class ScanCommandOutput extends CodeLocationOutput {
    public static final String DRY_RUN_RESULT_DIRECTORY = "data";
    public static final String SCAN_RESULT_DIRECTORY = "status";

    private final IntLogger logger;
    private final ScanCommand scanCommand;
    private final String executedScanCommand;
    private final Integer scanExitCode;

    public static ScanCommandOutput SUCCESS(NameVersion projectAndVersion, String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand) {
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(projectAndVersion, codeLocationName, expectedNotificationCount, Result.SUCCESS, logger, scanCommand, executedScanCommand, null, null, 0);
    }

    public static ScanCommandOutput FAILURE(NameVersion projectAndVersion, String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, String errorMessage, Exception exception) {
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(projectAndVersion, codeLocationName, expectedNotificationCount, Result.FAILURE, logger, scanCommand, executedScanCommand, errorMessage, exception, null);
    }

    public static ScanCommandOutput FAILURE(NameVersion projectAndVersion, String codeLocationName, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, int scanExitCode) {
        String errorMessage = String.format("The scan failed with return code: %d", scanExitCode);
        int expectedNotificationCount = calculateExpectedNotificationCount(scanCommand);
        return new ScanCommandOutput(projectAndVersion, codeLocationName, expectedNotificationCount, Result.FAILURE, logger, scanCommand, executedScanCommand, errorMessage, null, Integer.valueOf(scanExitCode));
    }

    private static int calculateExpectedNotificationCount(ScanCommand scanCommand) {
        if (scanCommand.isSnippetMatching()) {
            return 2;
        } else {
            return 1;
        }
    }

    private ScanCommandOutput(NameVersion projectAndVersion, String codeLocationName, int expectedNotificationCount, Result result, IntLogger logger, ScanCommand scanCommand, String executedScanCommand, String errorMessage,
        Exception exception, Integer scanExitCode) {
        super(result, projectAndVersion, codeLocationName, expectedNotificationCount, errorMessage, exception);
        this.logger = logger;
        this.scanCommand = scanCommand;
        this.executedScanCommand = executedScanCommand;
        this.scanExitCode = scanExitCode;
    }

    private Optional<File> getResultFile(String resultDirectoryName) {
        File resultDirectory = new File(scanCommand.getOutputDirectory(), resultDirectoryName);
        if (null != resultDirectory && resultDirectory.exists()) {
            File[] resultFiles = resultDirectory.listFiles((dir, name) -> FilenameUtils.wildcardMatchOnSystem(name, "*.json"));
            if (null != resultFiles && resultFiles.length == 1) {
                return Optional.of(resultFiles[0]);
            }
        }
        logger.error(String.format("Exactly 1 result file was not found in the result directory: %s", resultDirectory.getAbsolutePath()));
        return Optional.empty();
    }

    public boolean wasDryRun() {
        return scanCommand.isDryRun();
    }

    public Optional<File> getScanSummaryFile() {
        return getResultFile(SCAN_RESULT_DIRECTORY);
    }

    public Optional<File> getDryRunFile() {
        return getResultFile(DRY_RUN_RESULT_DIRECTORY);
    }

    public File getSpecificRunOutputDirectory() {
        return scanCommand.getOutputDirectory();
    }

    public String getScanTarget() {
        return scanCommand.getTargetPath();
    }

    public String getExecutedScanCommand() {
        return executedScanCommand;
    }

    public Optional<Integer> getScanExitCode() {
        return Optional.ofNullable(scanExitCode);
    }

}
