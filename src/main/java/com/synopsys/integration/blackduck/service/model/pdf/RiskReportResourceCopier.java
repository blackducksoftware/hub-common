/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model.pdf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.pdf.JarResourceCopier;

public class RiskReportResourceCopier extends JarResourceCopier {
    public static final String JSON_TOKEN_TO_REPLACE = "TOKEN_RISK_REPORT_JSON_TOKEN";
    public static final String RESOURCE_DIRECTORY = "riskreport/web/";
    public static final String RISK_REPORT_HTML_FILE_NAME = "riskreport.html";

    private final String destinationDirectory;

    public RiskReportResourceCopier(final String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public List<File> copy() throws IOException, URISyntaxException {
        return copy(RESOURCE_DIRECTORY, destinationDirectory);
    }

    @Override
    public List<String> findRelativePathFileList() {
        final List<String> relativePathList = new LinkedList<>();
        relativePathList.add("css/BlackDuckBomReport.css");
        relativePathList.add("images/Black_Duck_BD_logo.png");
        relativePathList.add(RISK_REPORT_HTML_FILE_NAME);
        relativePathList.addAll(findJavascriptFileList());
        return relativePathList;
    }

    private List<String> findJavascriptFileList() {
        final List<String> fileList = new LinkedList<>();
        final String parentDir = "js/";
        fileList.add(parentDir + "BlackDuckBomReportFunctions.js");
        fileList.add(parentDir + "BlackDuckRiskReport.js");
        fileList.add(parentDir + "jquery-3.1.1.min.js");
        fileList.add(parentDir + "Sortable.js");
        return fileList;
    }

}
