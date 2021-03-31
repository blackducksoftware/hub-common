/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;


public class ScanCommand {
    private final String scheme;
    private final String host;
    private final int port;

    private final String targetPath;
    private final String name;
    private final Set<String> excludePatterns;
    private final BlackDuckOnlineProperties blackDuckOnlineProperties;

    private final String blackDuckUsername;
    private final String blackDuckPassword;
    private final String blackDuckApiToken;
    private final File signatureScannerInstallDirectory;
    private final File outputDirectory;
    private final int scanMemoryInMegabytes;
    private final String scanCliOpts;
    private final String additionalScanArguments;
    private final boolean runInsecure;
    private final boolean dryRun;
    private final ProxyInfo proxyInfo;
    private final String projectName;
    private final String versionName;
    private final IndividualFileMatching individualFileMatching;
    private final boolean debug;
    private final boolean verbose;

    public ScanCommand(File signatureScannerInstallDirectory, File outputDirectory, boolean dryRun, ProxyInfo proxyInfo, String scanCliOpts, int scanMemoryInMegabytes, String scheme, String host, String blackDuckApiToken,
        String blackDuckUsername, String blackDuckPassword, int port, boolean runInsecure, String name, BlackDuckOnlineProperties blackDuckOnlineProperties, IndividualFileMatching individualFileMatching, Set<String> excludePatterns,
        String additionalScanArguments, String targetPath, boolean verbose, boolean debug, String projectName, String versionName) {
        this.signatureScannerInstallDirectory = signatureScannerInstallDirectory;
        this.outputDirectory = outputDirectory;
        this.dryRun = dryRun;
        this.proxyInfo = proxyInfo;
        this.scanCliOpts = scanCliOpts;
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        this.scheme = scheme;
        this.host = host;
        this.blackDuckApiToken = blackDuckApiToken;
        this.blackDuckUsername = blackDuckUsername;
        this.blackDuckPassword = blackDuckPassword;
        this.port = port;
        this.runInsecure = runInsecure;
        this.name = name;
        this.blackDuckOnlineProperties = blackDuckOnlineProperties;
        this.individualFileMatching = individualFileMatching;
        this.excludePatterns = excludePatterns;
        this.additionalScanArguments = additionalScanArguments;
        this.targetPath = targetPath;
        this.verbose = verbose;
        this.debug = debug;
        this.projectName = projectName;
        this.versionName = versionName;
    }

    public List<String> createCommandForProcessBuilder(IntLogger logger, ScanPaths scannerPaths, String specificRunOutputDirectoryPath) throws IllegalArgumentException, IntegrationException {
        List<String> cmd = new ArrayList<>();
        logger.debug("Using this java installation : " + scannerPaths.getPathToJavaExecutable());

        scannerPaths.addJavaAndOnePathArguments(cmd);

        if (proxyInfo.shouldUseProxy()) {
            populateProxyDetails(cmd);
        }

        populateScanCliOpts(cmd);

        cmd.add("-Xmx" + scanMemoryInMegabytes + "m");
        scannerPaths.addScanExecutableArguments(cmd);

        cmd.add("--no-prompt");

        if (!dryRun) {
            populateOnlineProperties(logger, cmd);
        } else {
            populateOfflineProperties(logger, specificRunOutputDirectoryPath, cmd);
        }

        if (verbose) {
            cmd.add("-v");
        }
        if (debug) {
            cmd.add("--debug");
        }

        cmd.add("--logDir");
        cmd.add(specificRunOutputDirectoryPath);

        // Only add the statusWriteDir option if Black Duck supports the statusWriteDir option
        // The scanStatusDirectoryPath is the same as the log directory path
        // The CLI will create a subdirectory for the status files
        cmd.add("--statusWriteDir");
        cmd.add(specificRunOutputDirectoryPath);

        populateProjectAndVersion(cmd);

        if (StringUtils.isNotBlank(name)) {
            cmd.add("--name");
            cmd.add(name);
        }

        populateExcludePatterns(cmd);

        if (null != individualFileMatching) {
            cmd.add("--individualFileMatching=" + individualFileMatching);
        }

        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        populateAdditionalScanArguments(cmd, parser);

        return cmd;
    }

    private void populateAdditionalScanArguments(List<String> cmd, ScanCommandArgumentParser parser) throws IntegrationException {
        List<String> arguments = parser.parse(additionalScanArguments);
        for (String argument : arguments) {
            if (StringUtils.isNotBlank(argument)) {
                cmd.add(argument);
            }
        }
    }

    private void populateExcludePatterns(List<String> cmd) {
        if (excludePatterns != null) {
            for (String exclusionPattern : excludePatterns) {
                if (StringUtils.isNotBlank(exclusionPattern)) {
                    cmd.add("--exclude");
                    cmd.add(exclusionPattern);
                }
            }
        }
    }

    private void populateProjectAndVersion(List<String> cmd) {
        if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(versionName)) {
            cmd.add("--project");
            cmd.add(projectName);
            cmd.add("--release");
            cmd.add(versionName);
        }
    }

    private void populateOfflineProperties(IntLogger logger, String specificRunOutputDirectoryPath, List<String> cmd) {
        logger.info("You have configured this signature scan to run in dry run mode - no results will be submitted to Black Duck.");
        blackDuckOnlineProperties.warnIfOnlineIsNeeded(logger::warn);

        // The dryRunWriteDir is the same as the log directory path
        // The CLI will create a subdirectory for the json files
        cmd.add("--dryRunWriteDir");
        cmd.add(specificRunOutputDirectoryPath);
    }

    private void populateOnlineProperties(IntLogger logger, List<String> cmd) {
        cmd.add("--scheme");
        cmd.add(scheme);
        cmd.add("--host");
        cmd.add(host);
        logger.debug("Using the Black Duck hostname : '" + host + "'");

        if (StringUtils.isEmpty(blackDuckApiToken)) {
            cmd.add("--username");
            cmd.add(blackDuckUsername);
        }

        int blackDuckPort = port;
        if (blackDuckPort > 0) {
            cmd.add("--port");
            cmd.add(Integer.toString(blackDuckPort));
        } else {
            logger.warn("Could not find a port to use for the Server.");
        }

        if (runInsecure) {
            cmd.add("--insecure");
        }

        blackDuckOnlineProperties.addOnlineCommands(cmd);
    }

    private void populateScanCliOpts(List<String> cmd) {
        if (StringUtils.isNotBlank(scanCliOpts)) {
            for (String scanOpt : scanCliOpts.split(" ")) {
                if (StringUtils.isNotBlank(scanOpt)) {
                    cmd.add(scanOpt);
                }
            }
        }
    }

    private void populateProxyDetails(List<String> cmd) {
        ProxyInfo blackDuckProxyInfo = proxyInfo;
        String proxyHost = blackDuckProxyInfo.getHost().orElse(null);
        int proxyPort = blackDuckProxyInfo.getPort();
        String proxyUsername = blackDuckProxyInfo.getUsername().orElse(null);
        String proxyPassword = blackDuckProxyInfo.getPassword().orElse(null);
        String proxyNtlmDomain = blackDuckProxyInfo.getNtlmDomain().orElse(null);
        String proxyNtlmWorkstation = blackDuckProxyInfo.getNtlmWorkstation().orElse(null);
        cmd.add("-Dhttp.proxyHost=" + proxyHost);
        cmd.add("-Dhttp.proxyPort=" + proxyPort);
        if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
            cmd.add("-Dhttp.proxyUser=" + proxyUsername);
            cmd.add("-Dhttp.proxyPassword=" + proxyPassword);
        } else {
            // CLI will ignore the proxy host and port if there are no credentials
            cmd.add("-Dhttp.proxyUser=user");
            cmd.add("-Dhttp.proxyPassword=password");
        }
        if (StringUtils.isNotBlank(proxyNtlmDomain)) {
            cmd.add("-Dhttp.auth.ntlm.domain=" + proxyNtlmDomain);
        }
        if (StringUtils.isNotBlank(proxyNtlmWorkstation)) {
            cmd.add("-Dblackduck.http.auth.ntlm.workstation=" + proxyNtlmWorkstation);
        }
    }

    public File getSignatureScannerInstallDirectory() {
        return signatureScannerInstallDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunInsecure() {
        return runInsecure;
    }

    public String getName() {
        return name;
    }

    public boolean isSnippetMatching() {
        return blackDuckOnlineProperties.isSnippetMatching();
    }

    public boolean isSnippetMatchingOnly() {
        return blackDuckOnlineProperties.isSnippetMatchingOnly();
    }

    public boolean isFullSnippetScan() {
        return blackDuckOnlineProperties.isFullSnippetScan();
    }

    public boolean isUploadSource() {
        return blackDuckOnlineProperties.isUploadSource();
    }

    public boolean isLicenseSearch() {
        return blackDuckOnlineProperties.isLicenseSearch();
    }

    public boolean isCopyrightSearch() {
        return blackDuckOnlineProperties.isCopyrightSearch();
    }

    public IndividualFileMatching getIndividualFileMatching() {
        return individualFileMatching;
    }

    public Set<String> getExcludePatterns() {
        return excludePatterns;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

}
