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

package org.kaaproject.kaa.server.common.log.shared.appender;

/**
 * The Enum to represent failures during log delivery.
 */
public enum LogDeliveryErrorCode {

    /** There are no appenders configured. */
    NO_APPENDERS_CONFIGURED,
    /** The appender internal error. */
    APPENDER_INTERNAL_ERROR,
    /** The connection error to log delivery destination system. */
    REMOTE_CONNECTION_ERROR,
    /** The internal error of log delivery destination system. */
    REMOTE_INTERNAL_ERROR
}
