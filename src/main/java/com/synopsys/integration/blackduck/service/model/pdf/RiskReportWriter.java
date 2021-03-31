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
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.exception.RiskReportException;
import com.synopsys.integration.blackduck.service.model.ReportData;

public class RiskReportWriter {
    public void createHtmlReportFiles(Gson gson, File outputDirectory, ReportData reportData) throws RiskReportException {
        try {
            RiskReportResourceCopier copier = new RiskReportResourceCopier(outputDirectory.getCanonicalPath());
            File htmlFile = null;
            try {
                List<File> writtenFiles = copier.copy();
                for (File file : writtenFiles) {
                    if (file.getName().equals(RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME)) {
                        htmlFile = file;
                        break;
                    }
                }
            } catch (URISyntaxException e) {
                throw new RiskReportException("Couldn't create the report: " + e.getMessage(), e);
            }
            if (htmlFile == null) {
                throw new RiskReportException("Could not find the file : " + RiskReportResourceCopier.RISK_REPORT_HTML_FILE_NAME
                                                  + ", the report files must not have been copied into the report directory.");
            }
            String htmlFileString = FileUtils.readFileToString(htmlFile, "UTF-8");
            String reportString = gson.toJson(reportData);
            htmlFileString = htmlFileString.replace(RiskReportResourceCopier.JSON_TOKEN_TO_REPLACE, reportString);
            FileUtils.writeStringToFile(htmlFile, htmlFileString, "UTF-8");
        } catch (IOException e) {
            throw new RiskReportException("Couldn't create the report: " + e.getMessage(), e);
        }
    }

}
