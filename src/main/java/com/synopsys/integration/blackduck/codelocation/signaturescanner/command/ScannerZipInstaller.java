/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.SignatureScannerClient;
import com.synopsys.integration.blackduck.keystore.KeyStoreHelper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.CleanupZipExpander;
import com.synopsys.integration.util.OperatingSystemType;

public class ScannerZipInstaller {
    public static final String DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli.zip";
    public static final String WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-windows.zip";
    public static final String MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX = "download/scan.cli-macosx.zip";

    public static final String BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY = "Black_Duck_Scan_Installation";
    public static final String VERSION_FILENAME = "blackDuckVersion.txt";

    private final IntLogger logger;
    private final SignatureScannerClient signatureScannerClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final ScanPathsUtility scanPathsUtility;
    private final KeyStoreHelper keyStoreHelper;
    private final HttpUrl blackDuckServerUrl;
    private final OperatingSystemType operatingSystemType;

    public ScannerZipInstaller(IntLogger logger, SignatureScannerClient signatureScannerClient, CleanupZipExpander cleanupZipExpander, ScanPathsUtility scanPathsUtility, KeyStoreHelper keyStoreHelper, HttpUrl blackDuckServerUrl,
        OperatingSystemType operatingSystemType) {
        if (null == blackDuckServerUrl) {
            throw new IllegalArgumentException("A Black Duck server url must be provided.");
        }

        this.logger = logger;
        this.signatureScannerClient = signatureScannerClient;
        this.cleanupZipExpander = cleanupZipExpander;
        this.scanPathsUtility = scanPathsUtility;
        this.keyStoreHelper = keyStoreHelper;
        this.blackDuckServerUrl = blackDuckServerUrl;
        this.operatingSystemType = operatingSystemType;
    }

    /**
     * The Black Duck Signature Scanner will be download if it has not
     * previously been downloaded or if it has been updated on the server. The
     * absolute path to the install location will be returned if it was
     * downloaded or found successfully, otherwise an Optional.empty will be
     * returned and the log will contain details concerning the failure.
     */
    public void installOrUpdateScanner(File installDirectory) throws BlackDuckIntegrationException {
        File scannerExpansionDirectory = new File(installDirectory, ScannerZipInstaller.BLACK_DUCK_SIGNATURE_SCANNER_INSTALL_DIRECTORY);
        scannerExpansionDirectory.mkdirs();

        File versionFile = null;
        try {
            versionFile = retrieveVersionFile(scannerExpansionDirectory);
        } catch (IOException e) {
            throw new BlackDuckIntegrationException("Trying to install the scanner but could not create the version file: " + e.getMessage(), e);
        }

        HttpUrl downloadUrl = getDownloadUrl();
        try {
            downloadIfModified(scannerExpansionDirectory, versionFile, downloadUrl);
        } catch (Exception e) {
            throw new BlackDuckIntegrationException("The Black Duck Signature Scanner could not be downloaded successfully: " + e.getMessage(), e);
        }

        logger.info("The Black Duck Signature Scanner downloaded/found successfully: " + installDirectory.getAbsolutePath());
    }

    private File retrieveVersionFile(File scannerExpansionDirectory) throws IOException {
        File versionFile = new File(scannerExpansionDirectory, ScannerZipInstaller.VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    private HttpUrl getDownloadUrl() throws BlackDuckIntegrationException {
        StringBuilder url = new StringBuilder(blackDuckServerUrl.string());
        if (!blackDuckServerUrl.string().endsWith("/")) {
            url.append("/");
        }

        if (OperatingSystemType.MAC == operatingSystemType) {
            url.append(ScannerZipInstaller.MAC_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else if (OperatingSystemType.WINDOWS == operatingSystemType) {
            url.append(ScannerZipInstaller.WINDOWS_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        } else {
            url.append(ScannerZipInstaller.DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
        }

        try {
            return new HttpUrl(url.toString());
        } catch (IntegrationException e) {
            throw new BlackDuckIntegrationException(String.format("The Black Duck Signature Scanner url (%s) is not valid.", url.toString()));
        }
    }

    private void downloadIfModified(File scannerExpansionDirectory, File versionFile, HttpUrl downloadUrl) throws IOException, IntegrationException, ArchiveException {
        long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        Request downloadRequest = new Request.Builder(downloadUrl).build();

        Optional<Response> optionalResponse = signatureScannerClient.executeGetRequestIfModifiedSince(downloadRequest, lastTimeDownloaded);
        if (!optionalResponse.isPresent()) {
            logger.debug("The Black Duck Signature Scanner has not been modified since it was last downloaded - skipping download.");
            return;
        }

        try (Response response = optionalResponse.get()) {
            logger.info("Downloading the Black Duck Signature Scanner.");
            try (InputStream responseStream = response.getContent()) {
                logger.info(String.format(
                    "If your Black Duck server has changed, the contents of %s may change which could involve deleting files - please do not place items in the expansion directory as this directory is assumed to be under blackduck-common control.",
                    scannerExpansionDirectory.getAbsolutePath()));
                cleanupZipExpander.expand(responseStream, scannerExpansionDirectory);
            }
            long lastModifiedOnServer = response.getLastModified();
            versionFile.setLastModified(lastModifiedOnServer);

            ScanPaths scanPaths = scanPathsUtility.determineSignatureScannerPaths(scannerExpansionDirectory.getParentFile());
            File javaExecutable = new File(scanPaths.getPathToJavaExecutable());
            File oneJar = new File(scanPaths.getPathToOneJar());
            File scanExecutable = new File(scanPaths.getPathToScanExecutable());
            javaExecutable.setExecutable(true);
            oneJar.setExecutable(true);
            scanExecutable.setExecutable(true);

            Certificate serverCertificate = signatureScannerClient.getServerCertificate();

            keyStoreHelper.updateKeyStoreWithServerCertificate(downloadUrl.url().getHost(), serverCertificate, scanPaths.getPathToCacerts());

            logger.info("Black Duck Signature Scanner downloaded successfully.");
        }
    }

}
