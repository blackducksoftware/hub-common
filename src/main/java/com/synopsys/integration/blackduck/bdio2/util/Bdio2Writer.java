/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.util;

import java.io.IOException;
import java.io.OutputStream;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.BdioWriter;
import com.blackducksoftware.bdio2.model.Component;
import com.synopsys.integration.blackduck.bdio2.model.Bdio2Document;
import com.synopsys.integration.blackduck.bdio2.model.Bdio2Project;

public class Bdio2Writer {
    public BdioWriter createBdioWriter(final OutputStream outputStream, final BdioMetadata bdioMetadata) {
        final BdioWriter.StreamSupplier streamSupplier = new BdioWriter.BdioFile(outputStream);
        return new BdioWriter(bdioMetadata, streamSupplier);
    }

    public void writeBdioDocument(final OutputStream outputStream, final Bdio2Document bdio2Document) throws IOException {
        final BdioWriter bdioWriter = createBdioWriter(outputStream, bdio2Document.getBdioMetadata());
        writeBdioDocument(bdioWriter, bdio2Document.getProject());
    }

    public void writeBdioDocument(final BdioWriter bdioWriter, Bdio2Project bdio2Project) throws IOException {
        bdioWriter.start();
        writeBdio2Project(bdioWriter, bdio2Project);
        bdioWriter.close();
    }

    private void writeBdio2Project(BdioWriter bdioWriter, Bdio2Project bdio2Project) throws IOException {
        for (Component component : bdio2Project.getComponents()) {
            bdioWriter.next(component);
        }

        // We put the project node at the end of the document to be more inline with the way Black Duck produces BDIO 2.
        bdioWriter.next(bdio2Project.getProject());

        for (final Bdio2Project subproject : bdio2Project.getSubprojects()) {
            writeBdio2Project(bdioWriter, subproject);
        }
    }
}
