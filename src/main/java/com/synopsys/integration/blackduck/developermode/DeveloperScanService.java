/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.developermode;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class DeveloperScanService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 30;
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private DeveloperModeBdio2Reader bdio2Reader;
    private DeveloperScanWaiter developerScanWaiter;
    private DeveloperModeBdio2Uploader bdio2Uploader;

    public DeveloperScanService(DeveloperModeBdio2Reader bdio2Reader, DeveloperModeBdio2Uploader bdio2Uploader, DeveloperScanWaiter developerScanWaiter) {
        this.bdio2Reader = bdio2Reader;
        this.developerScanWaiter = developerScanWaiter;
        this.bdio2Uploader = bdio2Uploader;
    }

    public List<DeveloperScanComponentResultView> performDeveloperScan(String userAgent, File bdio2File, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performDeveloperScan(userAgent, bdio2File, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<DeveloperScanComponentResultView> performDeveloperScan(String userAgent, File bdio2File, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        List<DeveloperModeBdioContent> developerModeBdioContentList = bdio2Reader.readBdio2File(bdio2File);
        return uploadFilesAndWait(userAgent, developerModeBdioContentList, timeoutInSeconds, waitIntervalInSeconds);
    }

    private List<DeveloperScanComponentResultView> uploadFilesAndWait(String userAgent, List<DeveloperModeBdioContent> bdioFiles, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        DeveloperModeBdioContent header = bdioFiles.stream()
                                              .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                              .findFirst()
                                              .orElseThrow(() -> new BlackDuckIntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<DeveloperModeBdioContent> remainingFiles = bdioFiles.stream()
                                                            .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                                            .collect(Collectors.toList());
        int count = remainingFiles.size();
        HttpUrl url = bdio2Uploader.start(userAgent, header);
        for (DeveloperModeBdioContent content : remainingFiles) {
            bdio2Uploader.append(url, userAgent, count, content);
        }
        bdio2Uploader.finish(url, userAgent, count);

        return developerScanWaiter.checkScanResult(url, timeoutInSeconds, waitIntervalInSeconds);
    }
}
