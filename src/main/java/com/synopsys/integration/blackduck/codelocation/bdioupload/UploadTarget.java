/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.NameVersion;

public class UploadTarget {
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final File uploadFile;
    private final String mediaType;

    public static UploadTarget createDefault(NameVersion projectAndVersion, String codeLocationName, File uploadFile) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, "application/ld+json");
    }

    public static UploadTarget createWithMediaType(NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) {
        return new UploadTarget(projectAndVersion, codeLocationName, uploadFile, mediaType);
    }

    private UploadTarget(NameVersion projectAndVersion, String codeLocationName, File uploadFile, String mediaType) throws IllegalArgumentException {
        if (StringUtils.isAnyBlank(projectAndVersion.getName(), projectAndVersion.getVersion())) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank project and version.");
        }
        if (StringUtils.isBlank(codeLocationName)) {
            throw new IllegalArgumentException("An UploadTarget must have a non-blank codeLocationName.");
        }

        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.uploadFile = uploadFile;
        this.mediaType = mediaType;
    }

    public NameVersion getProjectAndVersion() {
        return projectAndVersion;
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public String getMediaType() {
        return mediaType;
    }

}
