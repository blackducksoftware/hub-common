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
package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class BinaryScanCodeLocationCreationRequest extends CodeLocationCreationRequest<BinaryScanBatchOutput> {
    private final BinaryScanBatchRunner binaryScanBatchRunner;
    private final BinaryScanBatch binaryScanBatch;

    public BinaryScanCodeLocationCreationRequest(BinaryScanBatchRunner binaryScanBatchRunner, BinaryScanBatch binaryScanBatch) {
        this.binaryScanBatchRunner = binaryScanBatchRunner;
        this.binaryScanBatch = binaryScanBatch;
    }

    @Override
    public BinaryScanBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return binaryScanBatchRunner.executeUploads(binaryScanBatch);
    }

}