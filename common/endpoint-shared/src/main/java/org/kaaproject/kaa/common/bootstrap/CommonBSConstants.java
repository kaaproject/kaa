/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.common.bootstrap;

import org.kaaproject.kaa.common.Constants;

/**
 * Common Bootstrap Constants.
 */
public interface CommonBSConstants extends Constants { //NOSONAR

    public static final String BOOTSTRAP_DOMAIN = "BS"; //NOSONAR

    public static final String BOOTSTRAP_RESOLVE_COMMAND = "Resolve"; //NOSONAR
    public static final String BOOTSTRAP_RESOLVE_URI = URI_DELIM + BOOTSTRAP_DOMAIN + URI_DELIM //NOSONAR
            + BOOTSTRAP_RESOLVE_COMMAND;

    /** The Constant RESPONSE_TYPE. */
    public static final String RESPONSE_TYPE = "X-RESPONSETYPE"; //NOSONAR

    /** The Constant RESPONSE_TYPE_BOOTSTRAP. */
    public static final String RESPONSE_TYPE_BOOTSTRAP = "bootstrap"; //NOSONAR

    public static final String APPLICATION_TOKEN_ATTR_NAME = "Application-Token"; //NOSONAR
}
