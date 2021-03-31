/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.exception.MismatchedQuotesException;
import com.synopsys.integration.blackduck.exception.SignatureScannerInputException;
import com.synopsys.integration.exception.IntegrationArgumentException;
import com.synopsys.integration.exception.IntegrationException;

public class ScanCommandArgumentParser {
    public List<String> parse(String command) throws SignatureScannerInputException {
        if (!isParseable(command)) {
            return new LinkedList<>();
        }

        List<String> parsedArguments = new LinkedList<>();
        String[] splitBySpaces = command.split("\\s");
        boolean inQuotes = false;
        StringBuilder quotedArgument = null;
        for (String text : splitBySpaces) {
            if (text.startsWith("\"") && !inQuotes) {
                inQuotes = true;
                quotedArgument = new StringBuilder();
            }
            if (inQuotes) {
                if (quotedArgument.length() > 0) {
                    // we must be dealing with a quoted argument that has a space.  to preserve the original argument, add a space and then append the rest
                    quotedArgument.append(" ");
                }
                quotedArgument.append(text);
                if (text.endsWith("\"") && !text.endsWith("\\\"")) {
                    // we are only concerned with non-escaped quotes (escaped characters will just be passed along to scanner)
                    inQuotes = false;
                    parsedArguments.add(quotedArgument.toString());
                }
            } else {
                parsedArguments.add(text);
            }
        }

        validateParsedArguments(parsedArguments, command);

        return parsedArguments;
    }

    private boolean isParseable(String command) throws SignatureScannerInputException {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        validateCommand(command);
        return true;
    }

    private void validateCommand(String command) throws SignatureScannerInputException {
        ScanCommandQuoteParser quoteParser = new ScanCommandQuoteParser();
        if (!quoteParser.hasEvenNumberOfNonEscapedQuotes(command)) {
            throw new MismatchedQuotesException(String.format("Unable to parse signature scanner arguments due to unbalanced quotes in command: %s", command));
        }
    }

    private void validateParsedArguments(List<String> parsedArguments, String command) throws SignatureScannerInputException {
        String originalCheck = StringUtils.join(parsedArguments, " ");
        if (!originalCheck.equals(command)) {
            throw new SignatureScannerInputException("Unable to parse signature scanner arguments.  Please check your input for improper syntax, such as trailing spaces.");
        }
    }

}
