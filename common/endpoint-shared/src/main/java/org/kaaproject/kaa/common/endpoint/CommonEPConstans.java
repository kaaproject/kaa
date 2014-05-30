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

package org.kaaproject.kaa.common.endpoint;

import org.kaaproject.kaa.common.Constants;

/**
 * The Interface CommonEPConstans.
 */
public interface CommonEPConstans  extends Constants {

    /** The Constant ENDPOINT_DOMAIN. */
    public static final String ENDPOINT_DOMAIN = "EP";

    /** The Constant ENDPOINT_REGISTER_COMMAND. */
    public static final String ENDPOINT_REGISTER_COMMAND = "NewEPRegister";

    /** The Constant ENDPOINT_REGISTER_URI. */
    public static final String ENDPOINT_REGISTER_URI = URI_DELIM + ENDPOINT_DOMAIN + URI_DELIM
            + ENDPOINT_REGISTER_COMMAND;

    /** The Constant ENDPOINT_UPDATE_COMMAND. */
    public static final String ENDPOINT_UPDATE_COMMAND = "EPUpdate";

    /** The Constant ENDPOINT_UPDATE_URI. */
    public static final String ENDPOINT_UPDATE_URI = URI_DELIM + ENDPOINT_DOMAIN + URI_DELIM
            + ENDPOINT_UPDATE_COMMAND;

    /** The Constant SYNC_COMMAND. */
    public static final String SYNC_COMMAND = "Sync";

    /** The Constant SYNC_COMMAND. */
    public static final String LONG_SYNC_COMMAND = "LongSync";


    /** The Constant SYNC_URI. */
    public static final String SYNC_URI = URI_DELIM + ENDPOINT_DOMAIN + URI_DELIM + SYNC_COMMAND;

    /** The Constant SYNC_URI. */
    public static final String LONG_SYNC_URI = URI_DELIM + ENDPOINT_DOMAIN + URI_DELIM + LONG_SYNC_COMMAND;


    /** The Constant SIGNATURE_HEADER_NAME. */
    public static final String SIGNATURE_HEADER_NAME = "X-SIGNATURE";

    /** The Constant REQUEST_SIGNATURE_ATTR_NAME. */
    public static final String REQUEST_SIGNATURE_ATTR_NAME = "signature";

    /** The Constant REQUEST_KEY_ATTR_NAME. */
    public static final String REQUEST_KEY_ATTR_NAME = "requestKey";

    /** The Constant REQUEST_DATA_ATTR_NAME. */
    public static final String REQUEST_DATA_ATTR_NAME = "requestData";
}
