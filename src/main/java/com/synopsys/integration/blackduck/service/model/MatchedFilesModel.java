/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.component.ComponentMatchedFilesItemsFilePathView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentMatchedFilesView;

public class MatchedFilesModel {
    private final String path;
    private final String archiveContext;
    private final String fileName;
    private final String compositePathContext;
    private final Set<UsageType> usages;

    public MatchedFilesModel(final ComponentMatchedFilesView matchedFile) {
        final ComponentMatchedFilesItemsFilePathView pathView = matchedFile.getFilePath();
        path = pathView.getPath();
        archiveContext = pathView.getArchiveContext();
        fileName = pathView.getFileName();
        compositePathContext = pathView.getCompositePathContext();
        usages = new HashSet<UsageType>(matchedFile.getUsages());
    }

    public String getPath() {
        return path;
    }

    public String getArchiveContext() {
        return archiveContext;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCompositePathContext() {
        return compositePathContext;
    }

    public Set<UsageType> getUsages() {
        return usages;
    }
}
