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

package org.kaaproject.kaa.server.operations.service.logs;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;

/**
 * The Interface LogAppender.
 */
public interface LogAppender {

    /**
     * Set the name of this appender.
     * @param name the name of this appender
     */
    public void setName(String name);

    /**
     * Return the name of this appender.
     * @return the name of this appender
     */
    public String getName();

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getAppenderId();

    /**
     * Set the id of appender.
     *
     * @param appenderId the id of this appender
     */
    public void setAppenderId(String appenderId);

    /**
     * Release any resources allocated within the appender such as file
     * handles, network connections, etc.
     */
    public void close();

    /**
     * Log in <code>LogAppender</code> specific way.
     * @param logEventPack the pack of Log Events
     */
    public void doAppend(LogEventPack logEventPack);

    /**
     * Change parameters of log appender.
     *
     * @param appender the appender
     */
    void init(LogAppenderDto appender);
}
