/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.io.File;

import com.synopsys.integration.util.NameVersion;

public class BinaryScan {
    private final File binaryFile;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public BinaryScan(File binaryFile, String projectName, String projectVersion, String codeLocationName) {
        this.binaryFile = binaryFile;
        this.projectAndVersion = new NameVersion(projectName, projectVersion);
        this.codeLocationName = codeLocationName;
    }

    public File getBinaryFile() {
        return binaryFile;
    }

    public NameVersion getProjectAndVersion() {
        return projectAndVersion;
    }

    public String getProjectName() {
        return projectAndVersion.getName();
    }

    public String getProjectVersion() {
        return projectAndVersion.getVersion();
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

}
