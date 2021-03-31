/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.service.model.ScannerSplitStream;
import com.synopsys.integration.blackduck.service.model.StreamRedirectThread;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NameVersion;

public class ScanCommandCallable implements Callable<ScanCommandOutput> {
    private static final List<String> DRY_RUN_FILES_TO_KEEP = Arrays.asList("data");

    private final IntLogger logger;
    private final ScanPathsUtility scanPathsUtility;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScanCommand scanCommand;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final boolean onlineScan;
    private final boolean cleanupOutput;

    public ScanCommandCallable(IntLogger logger, ScanPathsUtility scanPathsUtility, IntEnvironmentVariables intEnvironmentVariables, ScanCommand scanCommand, boolean cleanupOutput) {
        this.logger = logger;
        this.scanPathsUtility = scanPathsUtility;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanCommand = scanCommand;
        this.projectAndVersion = new NameVersion(scanCommand.getProjectName(), scanCommand.getVersionName());
        this.codeLocationName = scanCommand.getName();
        this.onlineScan = !scanCommand.isDryRun();
        this.cleanupOutput = cleanupOutput;
    }

    @Override
    public ScanCommandOutput call() {
        String commandToExecute = "command_not_yet_configured";
        try {
            ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(scanCommand.getSignatureScannerInstallDirectory());

            List<String> cmd = scanCommand.createCommandForProcessBuilder(logger, scanPaths, scanCommand.getOutputDirectory().getAbsolutePath());
            cmd.add(scanCommand.getTargetPath());

            commandToExecute = createPrintableCommand(cmd);
            logger.info(String.format("Black Duck CLI command: %s", commandToExecute));

            File standardOutFile = scanPathsUtility.createStandardOutFile(scanCommand.getOutputDirectory());
            try (FileOutputStream outputFileStream = new FileOutputStream(standardOutFile)) {
                ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);
                ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                processBuilder.environment().putAll(intEnvironmentVariables.getVariables());

                if (onlineScan) {
                    prepareEnvironmentWithCredentials(processBuilder);
                }
                processBuilder.environment().put("BD_HUB_NO_PROMPT", "true");
                processBuilder.redirectErrorStream(true);

                Process blackDuckCliProcess = processBuilder.start();

                // The cli logs go the error stream for some reason
                StreamRedirectThread redirectThread = new StreamRedirectThread(blackDuckCliProcess.getInputStream(), splitOutputStream);
                redirectThread.start();

                int returnCode = executeScanProcess(blackDuckCliProcess, redirectThread);

                splitOutputStream.flush();

                logger.info(IOUtils.toString(blackDuckCliProcess.getInputStream(), StandardCharsets.UTF_8));

                logger.info("Black Duck Signature Scanner return code: " + returnCode);
                logger.info("You can view the logs at: '" + scanCommand.getOutputDirectory().getCanonicalPath() + "'");

                if (returnCode != 0) {
                    return ScanCommandOutput.FAILURE(projectAndVersion, codeLocationName, logger, scanCommand, commandToExecute, returnCode);
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("There was a problem scanning target '%s': %s", scanCommand.getTargetPath(), e.getMessage());
            return ScanCommandOutput.FAILURE(projectAndVersion, codeLocationName, logger, scanCommand, commandToExecute, errorMessage, e);
        }

        deleteFilesIfNeeded();

        return ScanCommandOutput.SUCCESS(projectAndVersion, codeLocationName, logger, scanCommand, commandToExecute);
    }

    private int executeScanProcess(Process blackDuckCliProcess, StreamRedirectThread redirectThread) throws InterruptedException {
        int returnCode = -1;
        try {
            returnCode = blackDuckCliProcess.waitFor();

            // the join method on the redirect thread will wait until the thread is dead
            // the thread will die when it reaches the end of stream and the run method is finished
            redirectThread.join();
        } finally {
            if (blackDuckCliProcess.isAlive()) {
                blackDuckCliProcess.destroy();
            }
            if (redirectThread.isAlive()) {
                redirectThread.interrupt();
            }
        }
        return returnCode;
    }

    private void prepareEnvironmentWithCredentials(ProcessBuilder processBuilder) {
        if (!StringUtils.isEmpty(scanCommand.getBlackDuckApiToken())) {
            processBuilder.environment().put("BD_HUB_TOKEN", scanCommand.getBlackDuckApiToken());
        } else {
            processBuilder.environment().put("BD_HUB_PASSWORD", scanCommand.getBlackDuckPassword());
        }
    }

    private void deleteFilesIfNeeded() {
        if (onlineScan && cleanupOutput) {
            FileUtils.deleteQuietly(scanCommand.getOutputDirectory());
        } else if (cleanupOutput) {
            // delete everything except dry run files
            File[] outputFiles = scanCommand.getOutputDirectory().listFiles();
            for (File outputFile : outputFiles) {
                if (!DRY_RUN_FILES_TO_KEEP.contains(outputFile.getName())) {
                    FileUtils.deleteQuietly(outputFile);
                }
            }
        }
    }

    /**
     * Code to mask passwords in the logs
     */
    private String createPrintableCommand(List<String> cmd) {
        List<String> cmdToOutput = new ArrayList<>();
        cmdToOutput.addAll(cmd);

        int passwordIndex = cmdToOutput.indexOf("--password");
        if (passwordIndex > -1) {
            // The User's password will be at the next index
            passwordIndex++;
        }

        int proxyPasswordIndex = -1;
        for (int commandIndex = 0; commandIndex < cmdToOutput.size(); commandIndex++) {
            String commandParameter = cmdToOutput.get(commandIndex);
            if (commandParameter.contains("-Dhttp.proxyPassword=")) {
                proxyPasswordIndex = commandIndex;
            }
        }

        maskIndex(cmdToOutput, passwordIndex);
        maskIndex(cmdToOutput, proxyPasswordIndex);

        return StringUtils.join(cmdToOutput, " ");
    }

    private void maskIndex(List<String> cmd, int indexToMask) {
        if (indexToMask > -1) {
            String cmdToMask = cmd.get(indexToMask);
            String[] maskedArray = new String[cmdToMask.length()];
            Arrays.fill(maskedArray, "*");
            cmd.set(indexToMask, StringUtils.join(maskedArray));
        }
    }

}
