/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;

import java.util.UUID;

import com.blackducksoftware.common.base.ExtraUUIDs;

/**
 * This functionality was copied from LegacyUtilities in the blackducksoftware/bdio repo. As of 3.0.0-beta.47 the class is package private.
 * A PR with a fix has been created: https://github.com/blackducksoftware/bdio/pull/14
 * // TODO: Remove this class once LegacyUtilities is made public.
 */
public class LegacyUtilitiesClone {
    /**
     * UUID name space identifier to use for name based UUIDs generated from legacy emitters.
     */
    // This is a version 3 UUID using the URL name space on the name
    // "https://blackducksoftware.github.io/bdio#LegacyEmitter"
    private static final UUID LEGACY_EMITTER_NS = ExtraUUIDs.fromString("d4bb9cdd-d89c-3a42-af03-393e0be722e4");

    /**
     * Legacy formats expected the BOM name (when used) to uniquely identify the graph, this method creates a URI to use
     * as the graph label from the BOM name.
     */
    public static String toNameUri(String name) {
        // IMPORTANT: This logic is permanent
        return ExtraUUIDs.toUriString(ExtraUUIDs.nameUUIDFromBytes(LEGACY_EMITTER_NS, name.toLowerCase(US).getBytes(UTF_8)));
    }
}
