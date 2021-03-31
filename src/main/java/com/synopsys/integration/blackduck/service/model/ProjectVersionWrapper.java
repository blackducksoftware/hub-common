/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;

public class ProjectVersionWrapper {
    private ProjectView projectView;
    private ProjectVersionView projectVersionView;

    public ProjectVersionWrapper() {
    }

    public ProjectVersionWrapper(final ProjectView projectView) {
        this.projectView = projectView;
        projectVersionView = null;
    }

    public ProjectVersionWrapper(final ProjectView projectView, final ProjectVersionView projectVersionView) {
        this.projectView = projectView;
        this.projectVersionView = projectVersionView;
    }

    public ProjectView getProjectView() {
        return projectView;
    }

    public void setProjectView(final ProjectView projectView) {
        this.projectView = projectView;
    }

    public ProjectVersionView getProjectVersionView() {
        return projectVersionView;
    }

    public void setProjectVersionView(final ProjectVersionView projectVersionView) {
        this.projectVersionView = projectVersionView;
    }
}
