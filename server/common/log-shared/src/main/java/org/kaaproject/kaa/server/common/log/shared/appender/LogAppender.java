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

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;

public interface LogAppender {

    /**
     * Set the name of this appender.
     * 
     * @param name
     *            the name of this appender
     */
    void setName(String name);

    /**
     * Return the name of this appender.
     * 
     * @return the name of this appender
     */
    String getName();

    /**
     * Set the id of appender.
     * 
     * @param appenderId
     *            the id of this appender
     */
    void setAppenderId(String appenderId);

    /**
     * Gets the id.
     * 
     * @return the id
     */
    String getAppenderId();

    /**
     * Sets the application token.
     * 
     * @param applicationToken
     *            the applicationToken to set
     */
    void setApplicationToken(String applicationToken);

    /**
     * Inits the appender.
     * 
     * @param appender
     *            the appender
     */
    void init(LogAppenderDto appender);

    /**
     * Do append.
     * 
     * @param logEventPack  the log event pack
     * @param listener      the listener
     */
    void doAppend(LogEventPack logEventPack, LogDeliveryCallback listener);

    /**
     * Check if appender support schema version
     * 
     * @param version
     *            the version of schema
     * @return true if appender supports specified schema version, false
     *         otherwise.
     */
    boolean isSchemaVersionSupported(int version);

    /**
     * Check if appender requires confirmation of data delivery
     * 
     * @return true if appender requires confirmation of data delivery, false
     *         otherwise.
     */
    boolean isDeliveryConfirmationRequired();

    /**
     * Release any resources allocated within the appender such as file handles,
     * network connections, etc.
     */
    void close();
}
