/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.bootstrap.service.http;

import org.kaaproject.kaa.server.common.server.http.DefaultHttpServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BootstrapServerInitializer.
 */
public class BootstrapServerInitializer extends DefaultHttpServerInitializer {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpServerInitializer.class);

    /**
     * Instantiates a new bootstrap server initializer.
     */
    public BootstrapServerInitializer() {
        super();
        LOG.info("Initializing Bootstrap server...");
    }
}
