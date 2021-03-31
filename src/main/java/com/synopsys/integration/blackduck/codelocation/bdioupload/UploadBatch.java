/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdioupload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UploadBatch {
    private final List<UploadTarget> uploadTargets = new ArrayList<>();

    public UploadBatch() {

    }

    public UploadBatch(UploadTarget... uploadTargets) {
        this.uploadTargets.addAll(Arrays.asList(uploadTargets));
    }

    public void addUploadTarget(final UploadTarget uploadTarget) {
        uploadTargets.add(uploadTarget);
    }

    public List<UploadTarget> getUploadTargets() {
        return uploadTargets;
    }

}
