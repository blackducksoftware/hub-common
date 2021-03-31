/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.developermode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.exception.IntegrationException;

public class RapidScanBdio2Reader {

    public List<DeveloperModeBdioContent> readBdio2File(File bdio2File) throws IntegrationException {
        validateBdioFile(bdio2File);
        List<DeveloperModeBdioContent> developerModeBdioContentList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(bdio2File)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileExtension = FilenameUtils.getExtension(entry.getName());
                if ("jsonld".equals(fileExtension)) {
                    developerModeBdioContentList.add(readEntryContent(zipFile, entry));
                }
            }
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Exception unzipping BDIO file. Path: %s", bdio2File.getAbsolutePath()), ex);
        }
        return developerModeBdioContentList;
    }

    private void validateBdioFile(File bdio2File) throws IllegalArgumentException {
        String absolutePath = bdio2File.getAbsolutePath();
        if (!bdio2File.isFile()) {
            throw new IllegalArgumentException(String.format("bdio file provided is not a file. Path: %s ", absolutePath));
        }
        if (!bdio2File.exists()) {
            throw new IllegalArgumentException(String.format("bdio file does not exist. Path: %s", absolutePath));
        }
        String fileExtension = FilenameUtils.getExtension(absolutePath);
        if (!"bdio".equals(fileExtension)) {
            throw new IllegalArgumentException(String.format("Unknown file extension. Cannot perform developer scan. Path: %s", absolutePath));
        }
    }

    private DeveloperModeBdioContent readEntryContent(ZipFile zipFile, ZipEntry entry) throws IntegrationException {
        String entryContent;
        byte[] buffer = new byte[1024];
        try (InputStream zipInputStream = zipFile.getInputStream(entry);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, length);
            }
            bufferedOutputStream.flush();
            entryContent = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Error reading entry %s", entry.getName()), ex);
        }
        return new DeveloperModeBdioContent(entry.getName(), entryContent);
    }

}
