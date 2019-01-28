/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public abstract class CodeLocationBatchOutput<T extends CodeLocationOutput> implements Iterable<T> {
    private final List<T> outputs = new ArrayList<>();

    public CodeLocationBatchOutput(List<T> outputs) {
        this.outputs.addAll(outputs);
    }

    public List<T> getOutputs() {
        return outputs;
    }

    public Set<String> getSuccessfulCodeLocationNames() {
        return getOutputs().stream()
                       .filter(output -> Result.SUCCESS == output.getResult())
                       .map(CodeLocationOutput::getCodeLocationName)
                       .filter(StringUtils::isNotBlank)
                       .collect(Collectors.toSet());
    }

    @Override
    public Iterator<T> iterator() {
        return outputs.iterator();
    }

}
