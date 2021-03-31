/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.List;

public class ScanPaths {
    private final String pathToJavaExecutable;
    private final String pathToCacerts;
    private final String pathToOneJar;
    private final String pathToScanExecutable;
    private final boolean managedByLibrary;

    public ScanPaths(final String pathToJavaExecutable, final String pathToCacerts, final String pathToOneJar, final String pathToScanExecutable, final boolean managedByLibrary) {
        this.pathToJavaExecutable = pathToJavaExecutable;
        this.pathToCacerts = pathToCacerts;
        this.pathToOneJar = pathToOneJar;
        this.pathToScanExecutable = pathToScanExecutable;
        this.managedByLibrary = managedByLibrary;
    }

    public void addJavaAndOnePathArguments(final List<String> cmd) {
        cmd.add(getPathToJavaExecutable());
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + getPathToOneJar());
    }

    public void addScanExecutableArguments(final List<String> cmd) {
        cmd.add("-jar");
        cmd.add(getPathToScanExecutable());
    }

    public String getPathToJavaExecutable() {
        return pathToJavaExecutable;
    }

    public String getPathToCacerts() {
        return pathToCacerts;
    }

    public String getPathToOneJar() {
        return pathToOneJar;
    }

    public String getPathToScanExecutable() {
        return pathToScanExecutable;
    }

    public boolean isManagedByLibrary() {
        return managedByLibrary;
    }

}
