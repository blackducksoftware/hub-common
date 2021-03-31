/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.ScanFailedException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class ScanCommandRunner {
    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScanPathsUtility scanPathsUtility;
    private final ExecutorService executorService;

    public ScanCommandRunner(IntLogger logger, IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility, ExecutorService executorService) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanPathsUtility = scanPathsUtility;
        this.executorService = executorService;
    }

    public List<ScanCommandOutput> executeScans(List<ScanCommand> scanCommands, boolean cleanupOutput) throws ScanFailedException {
        logger.info("Starting the Black Duck Signature Scan commands.");
        List<ScanCommandOutput> scanCommandOutputs = executeCommands(scanCommands, cleanupOutput);
        logger.info("Completed the Black Duck Signature Scan commands.");

        return scanCommandOutputs;
    }

    private List<ScanCommandOutput> executeCommands(List<ScanCommand> scanCommands, boolean cleanupOutput) throws ScanFailedException {
        List<ScanCommandOutput> scanCommandOutputs = new ArrayList<>();

        try {
            List<ScanCommandCallable> callables = createCallables(scanCommands, cleanupOutput);
            List<Future<ScanCommandOutput>> submitted = new ArrayList<>();
            for (ScanCommandCallable callable : callables) {
                submitted.add(executorService.submit(callable));
            }
            for (Future<ScanCommandOutput> future : submitted) {
                ScanCommandOutput scanCommandOutput = future.get();
                if (scanCommandOutput != null) {
                    scanCommandOutputs.add(scanCommandOutput);
                }
            }
        } catch (Exception e) {
            throw new ScanFailedException(String.format("Encountered a problem waiting for a scan to finish. %s", e.getMessage()), e);
        }

        return scanCommandOutputs;
    }

    private List<ScanCommandCallable> createCallables(List<ScanCommand> scanCommands, boolean cleanupOutput) {
        List<ScanCommandCallable> callables = scanCommands.stream().map(scanCommand -> new ScanCommandCallable(logger, scanPathsUtility, intEnvironmentVariables, scanCommand, cleanupOutput)).collect(Collectors.toList());

        return callables;
    }

}
