/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

public class ProjectVersionDescription {
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersionUri;

    public ProjectVersionDescription(final String projectName, final String projectVersionName, final String projectVersionUri) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersionUri = projectVersionUri;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersionUri() {
        return projectVersionUri;
    }

}
