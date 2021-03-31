/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.BlackDuckOnlineProperties;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.IndividualFileMatching;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.SnippetMatching;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ScanBatchBuilder extends IntegrationBuilder<ScanBatch> {
    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;
    public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

    private File installDirectory;
    private File outputDirectory;
    private boolean cleanupOutput;

    private int scanMemoryInMegabytes = DEFAULT_MEMORY_IN_MEGABYTES;
    private boolean dryRun;
    private boolean debug;
    private boolean verbose = true;
    private String scanCliOpts;
    private String additionalScanArguments;

    private SnippetMatching snippetMatching;
    private boolean uploadSource;
    private boolean licenseSearch;
    private boolean copyrightSearch;
    private IndividualFileMatching individualFileMatching;

    private HttpUrl blackDuckUrl;
    private String blackDuckUsername;
    private String blackDuckPassword;
    private String blackDuckApiToken;
    private ProxyInfo proxyInfo = ProxyInfo.NO_PROXY_INFO;
    private boolean alwaysTrustServerCertificate;

    private String projectName;
    private String projectVersionName;

    private List<ScanTarget> scanTargets = new ArrayList<>();

    @Override
    protected ScanBatch buildWithoutValidation() {
        BlackDuckOnlineProperties blackDuckOnlineProperties = new BlackDuckOnlineProperties(snippetMatching, uploadSource, licenseSearch, copyrightSearch);
        return new ScanBatch(installDirectory, outputDirectory, cleanupOutput, scanMemoryInMegabytes, dryRun, debug, verbose, scanCliOpts, additionalScanArguments,
            blackDuckOnlineProperties, individualFileMatching, blackDuckUrl, blackDuckUsername, blackDuckPassword, blackDuckApiToken, proxyInfo, alwaysTrustServerCertificate,
            projectName, projectVersionName, scanTargets);
    }

    @Override
    protected void validate(BuilderStatus builderStatus) {
        if (scanTargets == null || scanTargets.size() < 1) {
            builderStatus.addErrorMessage("At least one target path must be provided.");
        } else {
            for (ScanTarget scanTarget : scanTargets) {
                validateScanTarget(builderStatus, scanTarget);
            }
        }

        if (blackDuckUrl != null) {
            validateBlackDuckCredentials(builderStatus);
        }

        if (scanMemoryInMegabytes < MINIMUM_MEMORY_IN_MEGABYTES) {
            builderStatus.addErrorMessage(String.format("The minimum amount of memory for the scan is %d MB.", MINIMUM_MEMORY_IN_MEGABYTES));
        }

        if (!StringUtils.isAllBlank(projectName, projectVersionName) && (StringUtils.isBlank(projectName) || StringUtils.isBlank(projectVersionName))) {
            builderStatus.addErrorMessage("Both projectName and projectVersionName must be provided or omitted together");
        }

        if (blackDuckUrl != null && proxyInfo == null) {
            builderStatus.addErrorMessage("Must provide proxy info.");
        }
    }

    private void validateBlackDuckCredentials(BuilderStatus builderStatus) {
        if (StringUtils.isNotBlank(blackDuckApiToken)) {
            return;
        }

        if (StringUtils.isAnyBlank(blackDuckUsername, blackDuckPassword)) {
            builderStatus.addErrorMessage("Either an api token or a username and password is required.");
        }
    }

    private void validateScanTarget(BuilderStatus builderStatus, ScanTarget scanTarget) {
        try {
            new File(scanTarget.getPath()).getCanonicalPath();
        } catch (IOException e) {
            builderStatus.addErrorMessage(String.format("The target path: %s is not valid since its canonical path could not be determined: %s.", scanTarget.getPath(), e.getMessage()));
        }
        for (String exclusionPattern : scanTarget.getExclusionPatterns()) {
            if (!exclusionPattern.startsWith("/") || !exclusionPattern.endsWith("/") || exclusionPattern.contains("**")) {
                builderStatus.addErrorMessage("The exclusion pattern: " + exclusionPattern + " is not valid. An exclusion pattern must start and end with a forward slash (/) and may not contain double asterisks (**).");
            }
        }
    }

    public ScanBatchBuilder fromBlackDuckServerConfig(BlackDuckServerConfig blackDuckServerConfig) {
        if (null == blackDuckServerConfig) {
            proxyInfo = ProxyInfo.NO_PROXY_INFO;
            blackDuckUrl = null;
            blackDuckUsername = null;
            blackDuckPassword = null;
            blackDuckApiToken = null;
            alwaysTrustServerCertificate = false;
        } else {
            proxyInfo = blackDuckServerConfig.getProxyInfo();
            blackDuckUrl = blackDuckServerConfig.getBlackDuckUrl();
            if (blackDuckServerConfig.usingApiToken()) {
                blackDuckApiToken = blackDuckServerConfig.getApiToken().orElse(null);
            } else if (blackDuckServerConfig.getCredentials().isPresent()) {
                blackDuckUsername = blackDuckServerConfig.getCredentials().get().getUsername().orElse(null);
                blackDuckPassword = blackDuckServerConfig.getCredentials().get().getPassword().orElse(null);
            }
            alwaysTrustServerCertificate = blackDuckServerConfig.isAlwaysTrustServerCertificate();
        }
        return this;
    }

    public ScanBatchBuilder addTarget(ScanTarget scanTarget) {
        scanTargets.add(scanTarget);
        return this;
    }

    public ScanBatchBuilder addTargets(List<ScanTarget> scanTargets) {
        this.scanTargets.addAll(scanTargets);
        return this;
    }

    public ScanBatchBuilder projectAndVersionNames(String projectName, String projectVersionName) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        return this;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public ScanBatchBuilder installDirectory(File installDirectory) {
        this.installDirectory = installDirectory;
        return this;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public ScanBatchBuilder outputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public boolean isCleanupOutput() {
        return cleanupOutput;
    }

    public ScanBatchBuilder cleanupOutput(boolean cleanupOutput) {
        this.cleanupOutput = cleanupOutput;
        return this;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public ScanBatchBuilder scanMemoryInMegabytes(int scanMemoryInMegabytes) {
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        return this;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public ScanBatchBuilder dryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public ScanBatchBuilder debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public ScanBatchBuilder verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public ScanBatchBuilder scanCliOpts(String scanCliOpts) {
        this.scanCliOpts = scanCliOpts;
        return this;
    }

    public String getAdditionalScanArguments() {
        return additionalScanArguments;
    }

    public ScanBatchBuilder additionalScanArguments(String additionalScanArguments) {
        this.additionalScanArguments = additionalScanArguments;
        return this;
    }

    public SnippetMatching getSnippetMatching() {
        return snippetMatching;
    }

    public ScanBatchBuilder snippetMatching(SnippetMatching snippetMatching) {
        this.snippetMatching = snippetMatching;
        return this;
    }

    public boolean getUploadSource() {
        return uploadSource;
    }

    public ScanBatchBuilder uploadSource(SnippetMatching snippetMatching, boolean uploadSource) {
        snippetMatching(snippetMatching);
        this.uploadSource = uploadSource;
        return this;
    }

    public ScanBatchBuilder uploadSource(boolean uploadSource) {
        this.uploadSource = uploadSource;
        return this;
    }

    public boolean isLicenseSearch() {
        return licenseSearch;
    }

    public void licenseSearch(boolean licenseSearch) {
        this.licenseSearch = licenseSearch;
    }

    public boolean getCopyrightSearch() {
        return copyrightSearch;
    }

    public void copyrightSearch(boolean copyrightSearch) {
        this.copyrightSearch = copyrightSearch;
    }

    public IndividualFileMatching getIndividualFileMatching() {
        return individualFileMatching;
    }

    public void individualFileMatching(IndividualFileMatching individualFileMatching) {
        this.individualFileMatching = individualFileMatching;
    }

    public HttpUrl getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public ScanBatchBuilder blackDuckUrl(HttpUrl blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
        return this;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public ScanBatchBuilder blackDuckUsername(String blackDuckUsername) {
        this.blackDuckUsername = blackDuckUsername;
        return this;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public ScanBatchBuilder blackDuckPassword(String blackDuckPassword) {
        this.blackDuckPassword = blackDuckPassword;
        return this;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public ScanBatchBuilder blackDuckApiToken(String blackDuckApiToken) {
        this.blackDuckApiToken = blackDuckApiToken;
        return this;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public ScanBatchBuilder proxyInfo(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        return this;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public ScanBatchBuilder alwaysTrustServerCertificate(boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public List<ScanTarget> getScanTargets() {
        return scanTargets;
    }

    public ScanBatchBuilder simpleScanTargets(List<ScanTarget> scanTargets) {
        this.scanTargets = scanTargets;
        return this;
    }

}
