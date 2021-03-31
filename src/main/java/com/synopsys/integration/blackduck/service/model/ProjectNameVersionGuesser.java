/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class ProjectNameVersionGuesser {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public ProjectNameVersionGuess guessNameAndVersion(final String fullString) {
        String guessedName = "";
        String guessedVersion = "";

        int index = -1;
        if (fullString.contains("-")) {
            index = findIndexBeforeNumeric(fullString, "-", 0);
        } else if (fullString.contains(".")) {
            index = findIndexBeforeNumeric(fullString, ".", 0);
        }

        if (index > 0) {
            guessedName = fullString.substring(0, index);
            guessedVersion = fullString.substring(index + 1);
        }

        if (StringUtils.isBlank(guessedName) || StringUtils.isBlank(guessedVersion)) {
            guessedName = fullString;
            guessedVersion = getDefaultVersionGuess();
        }

        return new ProjectNameVersionGuess(guessedName, guessedVersion);
    }

    public String getDefaultVersionGuess() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String defaultVersionGuess = simpleDateFormat.format(new Date());
        return defaultVersionGuess;
    }

    private int findIndexBeforeNumeric(final String fullString, final String separator, final int fromIndex) {
        final int index = fullString.indexOf(separator, fromIndex);
        if (index == -1) {
            return -1;
        } else if (index + 1 < fullString.length() && StringUtils.isNumeric(fullString.substring(index + 1, index + 2))) {
            return index;
        } else {
            return findIndexBeforeNumeric(fullString, separator, index + 1);
        }
    }

}
