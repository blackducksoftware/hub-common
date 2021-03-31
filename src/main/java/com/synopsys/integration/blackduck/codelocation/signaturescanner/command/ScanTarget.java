/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class ScanTarget {
    private final String path;
    private final Set<String> exclusionPatterns;
    private final String codeLocationName;
    private final String outputDirectoryPath;
    private final boolean outputDirectoryPathAbsolute;

    public static ScanTarget createBasicTarget(String path) {
        return new ScanTarget(path, null, null, null, false);
    }

    public static ScanTarget createBasicTarget(String path, String codeLocationName) {
        return new ScanTarget(path, null, codeLocationName, null, false);
    }

    public static ScanTarget createBasicTarget(String path, Set<String> exclusionPatterns, String codeLocationName) {
        return new ScanTarget(path, exclusionPatterns, codeLocationName, null, false);
    }

    private ScanTarget(String path, Set<String> exclusionPatterns, String codeLocationName, String outputDirectoryPath, boolean outputDirectoryPathAbsolute) {
        this.path = path;
        this.exclusionPatterns = exclusionPatterns;
        this.codeLocationName = codeLocationName;
        this.outputDirectoryPath = outputDirectoryPath;
        this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getExclusionPatterns() {
        return Optional.ofNullable(exclusionPatterns).orElse(Collections.emptySet())
                   .stream()
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public String getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public boolean isOutputDirectoryPathAbsolute() {
        return outputDirectoryPathAbsolute;
    }

    public File determineCommandOutputDirectory(ScanPathsUtility scanPathsUtility, File outputDirectory) throws BlackDuckIntegrationException {
        if (StringUtils.isNotBlank(getOutputDirectoryPath())) {
            File commandOutputDirectory;
            if (isOutputDirectoryPathAbsolute()) {
                commandOutputDirectory = new File(getOutputDirectoryPath());
            } else {
                commandOutputDirectory = new File(outputDirectory, getOutputDirectoryPath());
            }
            commandOutputDirectory.mkdirs();
            return commandOutputDirectory;
        } else {
            return scanPathsUtility.createSpecificRunOutputDirectory(outputDirectory);
        }
    }

    public static class Builder {
        private String path;
        private Set<String> exclusionPatterns;
        private String codeLocationName;
        private String outputDirectoryPath;
        private boolean outputDirectoryPathAbsolute;

        public Builder(String path) {
            this.path = path;
        }

        public ScanTarget build() {
            return new ScanTarget(path, exclusionPatterns, codeLocationName, outputDirectoryPath, outputDirectoryPathAbsolute);
        }

        public String getPath() {
            return path;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Set<String> getExclusionPatterns() {
            return exclusionPatterns;
        }

        public Builder exclusionPatterns(Set<String> exclusionPatterns) {
            this.exclusionPatterns = exclusionPatterns;
            return this;
        }

        public String getCodeLocationName() {
            return codeLocationName;
        }

        public Builder codeLocationName(String codeLocationName) {
            this.codeLocationName = codeLocationName;
            return this;
        }

        public String getOutputDirectoryPath() {
            return outputDirectoryPath;
        }

        public Builder outputDirectoryPath(String outputDirectoryPath) {
            this.outputDirectoryPath = outputDirectoryPath;
            return this;
        }

        public boolean isOutputDirectoryPathAbsolute() {
            return outputDirectoryPathAbsolute;
        }

        public Builder outputDirectoryPathAbsolute(boolean outputDirectoryPathAbsolute) {
            this.outputDirectoryPathAbsolute = outputDirectoryPathAbsolute;
            return this;
        }

        public Builder outputDirectoryPath(String outputDirectoryPath, boolean isAbsolute) {
            this.outputDirectoryPath = outputDirectoryPath;
            outputDirectoryPathAbsolute = isAbsolute;
            return this;
        }
    }
}
