/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommand;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandRunner;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPaths;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanPathsUtility;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.OperatingSystemType;

public class ScanBatchRunner {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScannerZipInstaller scannerZipInstaller;
    private final ScanPathsUtility scanPathsUtility;
    private final ScanCommandRunner scanCommandRunner;
    private final File defaultInstallDirectory;

    public static ScanBatchRunner createDefault(IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, IntEnvironmentVariables intEnvironmentVariables, ExecutorService executorService) {
        OperatingSystemType operatingSystemType = OperatingSystemType.determineFromSystem();
        ScanPathsUtility scanPathsUtility = new ScanPathsUtility(logger, intEnvironmentVariables, operatingSystemType);
        ScanCommandRunner scanCommandRunner = new ScanCommandRunner(logger, intEnvironmentVariables, scanPathsUtility, executorService);

        return ScanBatchRunner.createDefault(logger, blackDuckHttpClient, intEnvironmentVariables, scanPathsUtility, operatingSystemType, scanCommandRunner);
    }

    public static ScanBatchRunner createDefault(IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, IntEnvironmentVariables intEnvironmentVariables, ScanPathsUtility scanPathsUtility,
        OperatingSystemType operatingSystemType, ScanCommandRunner scanCommandRunner) {
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(blackDuckHttpClient);
        KeyStoreHelper keyStoreHelper = new KeyStoreHelper(logger);
        ScannerZipInstaller scannerZipInstaller = new ScannerZipInstaller(logger, signatureScannerClient, cleanupZipExpander, scanPathsUtility, keyStoreHelper, blackDuckHttpClient.getBaseUrl(), operatingSystemType);

        return new ScanBatchRunner(intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public static ScanBatchRunner createWithNoInstaller(IntEnvironmentVariables intEnvironmentVariables, File defaultInstallDirectory, ScanPathsUtility scanPathsUtility,
        ScanCommandRunner scanCommandRunner) {
        return new ScanBatchRunner(intEnvironmentVariables, null, scanPathsUtility, scanCommandRunner, defaultInstallDirectory);
    }

    public static ScanBatchRunner createComplete(IntEnvironmentVariables intEnvironmentVariables, ScannerZipInstaller scannerZipInstaller, ScanPathsUtility scanPathsUtility,
        ScanCommandRunner scanCommandRunner) {
        return new ScanBatchRunner(intEnvironmentVariables, scannerZipInstaller, scanPathsUtility, scanCommandRunner, null);
    }

    public ScanBatchRunner(IntEnvironmentVariables intEnvironmentVariables, ScannerZipInstaller scannerZipInstaller, ScanPathsUtility scanPathsUtility, ScanCommandRunner scanCommandRunner,
        File defaultInstallDirectory) {
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scannerZipInstaller = scannerZipInstaller;
        this.scanPathsUtility = scanPathsUtility;
        this.scanCommandRunner = scanCommandRunner;
        this.defaultInstallDirectory = defaultInstallDirectory;
    }

    public ScanBatchOutput executeScans(ScanBatch scanBatch) throws BlackDuckIntegrationException {
        if (scannerZipInstaller != null) {
            // if an installer is specified, it will be used to install/update the scanner
            File installDirectory = scanBatch.getSignatureScannerInstallDirectory();
            if (!installDirectory.exists()) {
                scannerZipInstaller.installOrUpdateScanner(installDirectory);
            } else {
                try {
                    ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(installDirectory);
                    if (scanPaths.isManagedByLibrary()) {
                        scannerZipInstaller.installOrUpdateScanner(installDirectory);
                    }
                } catch (BlackDuckIntegrationException e) {
                    // a valid scanPaths could not be found so we will need to attempt an install
                    scannerZipInstaller.installOrUpdateScanner(installDirectory);
                }
            }
        }

        List<ScanCommand> scanCommands = scanBatch.createScanCommands(defaultInstallDirectory, scanPathsUtility, intEnvironmentVariables);
        List<ScanCommandOutput> scanCommandOutputs = scanCommandRunner.executeScans(scanCommands, scanBatch.isCleanupOutput());
        return new ScanBatchOutput(scanCommandOutputs);
    }

}
