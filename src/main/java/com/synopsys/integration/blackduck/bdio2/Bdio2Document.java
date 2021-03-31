/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.List;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Project project;
    private final List<Component> components;

    public Bdio2Document(final BdioMetadata bdioMetadata, final Project project, final List<Component> components) {
        this.bdioMetadata = bdioMetadata;
        this.project = project;
        this.components = components;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Project getProject() {
        return project;
    }

    public List<Component> getComponents() {
        return components;
    }
}
