/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.exception;

import org.kaaproject.kaa.server.common.dao.EndpointRegistrationService;

/**
 * A checked exception to be thrown by {@link EndpointRegistrationService}.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public class EndpointRegistrationServiceException extends Exception {

    private static final long serialVersionUID = 1000L;

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause The exception cause
     */
    public EndpointRegistrationServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified cause and detail message.
     *
     * @param message The detail message
     * @param cause The exception cause
     */
    public EndpointRegistrationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
