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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;

/**
 * Message about status of log delivery for particular log upload request.
 */
public class LogDeliveryMessage {

    /** The request id. */
    private final Integer requestId;

    /** The success. */
    private final boolean success;

    /** The error code. */
    private final LogDeliveryErrorCode errorCode;

    /**
     * Instantiates a new log event delivery message.
     *
     * @param requestId
     *            the request id
     * @param success
     *            the success
     */
    public LogDeliveryMessage(Integer requestId, boolean success) {
        this(requestId, success, null);
    }

    /**
     * Instantiates a new log event delivery message.
     *
     * @param requestId
     *            the request id
     * @param success
     *            the success
     * @param errorCode
     *            the error code
     */
    public LogDeliveryMessage(Integer requestId, boolean success, LogDeliveryErrorCode errorCode) {
        this.requestId = requestId;
        this.success = success;
        this.errorCode = errorCode;
    }

    /**
     * Gets the request id.
     *
     * @return the request id
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Checks if is success.
     *
     * @return true, if is success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public LogDeliveryErrorCode getErrorCode() {
        return errorCode;
    }
}
